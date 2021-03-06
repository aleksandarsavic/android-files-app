package com.afterlogic.aurora.drive.data.modules.files.repository;

import android.content.Context;
import android.net.Uri;

import com.afterlogic.aurora.drive.core.common.logging.MyLog;
import com.afterlogic.aurora.drive.core.common.rx.Observables;
import com.afterlogic.aurora.drive.core.common.util.FileUtil;
import com.afterlogic.aurora.drive.core.common.util.IOUtil;
import com.afterlogic.aurora.drive.data.common.cache.SharedObservableStore;
import com.afterlogic.aurora.drive.data.common.mapper.BiMapper;
import com.afterlogic.aurora.drive.data.common.mapper.Mapper;
import com.afterlogic.aurora.drive.data.common.mapper.MapperUtil;
import com.afterlogic.aurora.drive.data.common.network.SessionManager;
import com.afterlogic.aurora.drive.data.common.repository.Repository;
import com.afterlogic.aurora.drive.data.modules.files.mapper.general.FilesMapperFactory;
import com.afterlogic.aurora.drive.data.modules.files.model.db.OfflineFileInfoEntity;
import com.afterlogic.aurora.drive.data.modules.files.service.FilesLocalService;
import com.afterlogic.aurora.drive.model.AuroraFile;
import com.afterlogic.aurora.drive.model.AuroraSession;
import com.afterlogic.aurora.drive.model.FileInfo;
import com.afterlogic.aurora.drive.model.OfflineType;
import com.afterlogic.aurora.drive.model.Progressible;
import com.afterlogic.aurora.drive.model.Storage;
import com.afterlogic.aurora.drive.model.error.FileAlreadyExistError;
import com.afterlogic.aurora.drive.model.error.FileNotExistError;
import com.annimon.stream.Stream;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.inject.Named;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;

import static com.afterlogic.aurora.drive.data.modules.files.FilesDataModule.CACHE_DIR;
import static com.afterlogic.aurora.drive.data.modules.files.FilesDataModule.DOWNLOADS_DIR;
import static com.afterlogic.aurora.drive.data.modules.files.FilesDataModule.OFFLINE_DIR;

/**
 * Created by sashka on 19.10.16.<p/>
 * mail: sunnyday.development@gmail.com
 */
public class FileRepositoryImpl extends Repository implements FilesRepository {

    private final static String FILES = "files";

    private final Context mAppContext;
    private final FileSubRepository subRepo;
    private final FilesLocalService mLocalService;
    private final SessionManager sessionManager;

    private final File mCacheDir;
    private final File mOfflineDir;
    private final File mDownloadsDir;

    private final Mapper<AuroraFile, OfflineFileInfoEntity> mOfflineToBlMapper;

    private FileRepositoryImpl(
            SharedObservableStore cache,
            Context appContext,
            FileSubRepository fileSubRepo,
            FilesLocalService localService,
            File cacheDir,
            File offlineDir,
            File downloadsDir,
            FilesMapperFactory mapperFactory,
            SessionManager sessionManager
    ) {
        super(cache, FILES);
        this.sessionManager = sessionManager;
        mAppContext = appContext;
        subRepo = fileSubRepo;
        mLocalService = localService;
        mCacheDir = cacheDir;
        mOfflineDir = offlineDir;
        mDownloadsDir = downloadsDir;

        BiMapper<AuroraFile, OfflineFileInfoEntity, File> offlineToBl = mapperFactory.offlineToBl();
        mOfflineToBlMapper = offlineFileInfo -> offlineToBl.map(offlineFileInfo, mOfflineDir);

    }

    @Override
    public Single<List<Storage>> getAvailableStorages() {
        return subRepo.getAvailableStorages();
    }

    @Override
    public Single<List<AuroraFile>> getFiles(AuroraFile folder) {
        return subRepo.getFiles(folder)
                .map(this::sortFiles);
    }

    @Override
    public Single<List<AuroraFile>> getFiles(AuroraFile folder, String pattern) {
        return subRepo.getFiles(folder, pattern)
                .map(this::sortFiles);
    }

    @Override
    public Single<Uri> getFileThumbnail(AuroraFile file) {
        return subRepo.getFileThumbnail(file);
    }

    @Override
    public Single<Uri> viewFile(AuroraFile file) {
        return subRepo.viewFile(file);
    }

    @Override
    public Completable createFolder(AuroraFile file) {
        return subRepo.createFolder(file);
    }

    @Override
    public final Single<AuroraFile> rename(AuroraFile file, String newName) {
        AuroraFile newFile = file.clone();
        newFile.setName(newName);
        return checkFileExisting(newFile)
                .andThen(Completable.error(new FileAlreadyExistError(newFile)))
                .compose(Observables.completeOnError(FileNotExistError.class))
                .andThen(subRepo.rename(file, newName))
                .andThen(checkFile(newFile));
    }

    @Override
    public Completable checkFileExisting(AuroraFile file) {
        return subRepo.checkFileExisting(file);
    }

    @Override
    public Single<AuroraFile> checkFile(AuroraFile file) {
        return subRepo.checkFile(file);
    }

    @Override
    public final Completable delete(AuroraFile files) {
        return delete(Collections.singletonList(files));
    }

    @Override
    public final Completable delete(List<AuroraFile> files) {
        return Completable.defer(() -> {
            String type = files.get(0).getType();

            checkFilesType(type, files);

            return subRepo.delete(type, files)
                    .andThen(Completable.defer(() -> Stream.of(files)
                            .map(it -> setOffline(it, false))
                            .collect(Observables.Collectors.concatCompletable())
                    ));
        });
    }

    @Override
    public final Observable<Progressible<File>> downloadOrGetOffline(AuroraFile file, File target) {
        return subRepo.checkFile(file)
                .onErrorResumeNext(error -> getOfflineFile(file.getPathSpec())
                        .flatMap(offlineFile -> offlineFile.getLastModified() != -1 ? Maybe.just(offlineFile) : Maybe.empty())
                        .switchIfEmpty(Maybe.error(error))
                        .toSingle()
                )
                .flatMapObservable(checked -> {
                    boolean isOffline = !mLocalService.get(file.getPathSpec()).isEmpty().blockingGet();

                    if (isOffline){
                        return checkOfflineAndDownload(checked, target);
                    } else {
                        return checkCacheAndDownload(checked, target);
                    }

                })
                .startWith(new Progressible<>(null, -1, -1, file.getName(), false));
    }

    @Override
    public final Observable<Progressible<AuroraFile>> uploadFile(AuroraFile folder, Uri file) {
        return Observable.defer(() -> {
            FileInfo fileInfo = FileUtil.fileInfo(file, mAppContext);

            //check file
            //noinspection ConstantConditions
            AuroraFile checkFile = AuroraFile.create(folder, fileInfo.getName(), false);
            return checkFileExisting(checkFile)
                    .andThen(Completable.error(new FileAlreadyExistError(checkFile)))
                    .compose(Observables.completeOnError(FileNotExistError.class))
                    //upload
                    .andThen(subRepo.uploadFileToServer(folder, fileInfo))
                    .<Progressible<AuroraFile>>flatMap(progress -> {
                        if (!progress.isDone()){
                            return Observable.just(progress.map(null));
                        } else {
                            return checkFile(AuroraFile.create(folder, fileInfo.getName(), false))
                                    .map(progress::map)
                                    .toObservable()
                                    .startWith(new Progressible<>(null, progress.getMax(), progress.getProgress(), progress.getName(), false));
                        }
                    })
                    .startWith(new Progressible<>(null, -1, -1, checkFile.getName(), false));
        });
    }

    @Override
    public Observable<Progressible<AuroraFile>> rewriteFile(AuroraFile file, Uri fileUri) {return Observable.defer(() -> {
        FileInfo fileInfo = FileUtil.fileInfo(fileUri, mAppContext);

        AuroraFile folder = file.getParentFolder();
        //check file and delete previous
        return checkFileExisting(file)
                .andThen(delete(file))
                .compose(Observables.completeOnError(FileNotExistError.class))
                //upload
                .andThen(subRepo.uploadFileToServer(folder, fileInfo))
                .flatMap(progress -> {
                    if (!progress.isDone()){
                        return Observable.just(progress.map(null));
                    } else {
                        //noinspection ConstantConditions
                        return checkFile(AuroraFile.create(folder, fileInfo.getName(), false))
                                .map(progress::map)
                                .toObservable()
                                .startWith(new Progressible<>(null, -1, 0, progress.getName(), false));
                    }
                });
    });
    }

    @Override
    public Completable setOffline(AuroraFile file, boolean offline) {
        return Completable.defer(() -> {
            if (offline){
                OfflineFileInfoEntity entity = new OfflineFileInfoEntity(file.getPathSpec(), OfflineType.OFFLINE.toString(), -1);
                return mLocalService.addOffline(entity)
                        .doOnComplete(() -> {
                            File cached = new File(mCacheDir, file.getPathSpec());
                            if (cached.exists() && !cached.delete()){
                                MyLog.majorException(new IOException("Can't delete file."));
                            }
                        });
            } else {
                return mLocalService.removeOffline(file.getPathSpec())
                        .doOnComplete(() -> {
                            File localFile = new File(mOfflineDir, file.getPathSpec());
                            if (localFile.exists() && !localFile.delete()){
                                MyLog.majorException(new IOException("Can't delete file."));
                            }
                        });
            }
        });
    }

    @Override
    public Maybe<AuroraFile> getOfflineFile(String pathSpec) {
        return mLocalService.get(pathSpec)
                .map(mOfflineToBlMapper::map);
    }

    @Override
    public Single<List<AuroraFile>> getOfflineFiles() {
        return mLocalService.getOffline()
                .map(offline -> {
                    List<AuroraFile> files = MapperUtil.listOrEmpty(offline, mOfflineToBlMapper);
                    Collections.sort(files, FileUtil.AURORA_FILE_COMPARATOR);
                    return files;
                });
    }

    @Override
    public Single<Boolean> getOfflineStatus(AuroraFile file) {
        return mLocalService.get(file.getPathSpec())
                .isEmpty()
                .map(empty -> !empty);

    }

    @Override
    public Completable clearOfflineData() {
        return mLocalService.clear();
    }

    @Override
    public Single<String> createPublicLink(AuroraFile file) {
        return subRepo.createPublicLink(file)
                .map(link -> {

                    AuroraSession session = sessionManager.getSession();

                    if (session == null) {
                        throw new IllegalStateException("Not authorized.");
                    }

                    if (link.startsWith("http://localhost")){
                        link = link.substring(16);
                    } else if (link.startsWith("https://localhost")){
                        link = link.substring(17);
                    }

                    if (!link.startsWith("http")) {

                        String domain = session.getDomain().toString();

                        link = domain + link;
                    }

                    return link;
                });
    }

    @Override
    public Completable deletePublicLink(AuroraFile file) {
        return subRepo.deletePublicLink(file);
    }

    @Override
    public Completable replaceFiles(AuroraFile targetFolder, List<AuroraFile> files) {
        return Completable.defer(() -> {
            checkFilesHasOneType(files);
            return subRepo.replaceFiles(targetFolder, files);
        });
    }

    @Override
    public Completable copyFiles(AuroraFile targetFolder, List<AuroraFile> files) {
        return Completable.defer(() -> {
            checkFilesHasOneType(files);
            return subRepo.copyFiles(targetFolder, files);
        });
    }

    // TODO: Replace checking is to downloads folder to interactors
    private Observable<Progressible<File>> checkOfflineAndDownload(
            AuroraFile checked, File target) {

        boolean toDownloads = target.getPath().startsWith(mDownloadsDir.getPath());

        File offlineFile = new File(mOfflineDir, checked.getPathSpec());
        boolean actual = offlineFile.exists() &&
                offlineFile.lastModified() >= checked.getLastModified();

        if (!toDownloads && actual){

            Progressible<File> progressibleOffline = new Progressible<>(
                    offlineFile, 0, 0, checked.getName(), true);
            return Observable.just(progressibleOffline);

        } else {

            Observable<Progressible<File>> download = downloadFile(checked, target)
                    .map(progress -> {
                        if (toDownloads && progress.getProgress() > 0) {
                            progress.setProgress((long) (progress.getProgress() * 0.9f));
                        }
                        return progress;
                    });

            Observable<Progressible<File>> copyFile = copyFile(
                    checked.getName(), offlineFile, target
            ) // copyFile
                    .map(p -> {
                        if (toDownloads && p.getProgress() > 0) {
                            p.setProgress((long) (p.getMax() * 0.9f + p.getProgress() * 0.1f));
                        }
                        return p;
                    });

            return Observable.concat(
                    actual ? Observable.empty() : download,
                    toDownloads ? copyFile : Observable.empty()
            );

        }
    }

    // TODO: Copy file from cache to target dir
    private Observable<Progressible<File>> checkCacheAndDownload(AuroraFile file, File target) {

        boolean toDownloads = target.getPath().startsWith(mDownloadsDir.getPath());

        File localActual = new File(mCacheDir, file.getPathSpec());

        if (localActual.exists() && localActual.lastModified() == file.getLastModified()){

            if (toDownloads){

                return copyFile(file.getName(), localActual, target);

            } else {
                Progressible<File> done = new Progressible<>(
                        localActual, 0, 0, file.getName(), true);
                return Observable.just(done);

            }

        } else {

            return downloadFile(file, target);

        }
    }

    private Observable<Progressible<File>> copyFile(String progressName, File source, File target) {
        return Observable.create(emitter -> {

            FileInputStream fis = null;

            try {

                fis = new FileInputStream(source);
                FileUtil.writeFile(fis, target, read -> emitter.onNext(
                        new Progressible<>(
                                null, source.length(), read, progressName, false)
                ));

            } catch (Exception e) {

                emitter.onError(e);
                return;

            } finally {

                IOUtil.closeQuietly(fis);

            }

            emitter.onNext(new Progressible<>(
                    target, source.length(), source.length(), progressName, true));

            emitter.onComplete();

        });
    }

    private Observable<Progressible<File>> downloadFile(AuroraFile file, File target){

        AtomicBoolean finished = new AtomicBoolean(false);

        Observable<Progressible<File>> request = subRepo.downloadFileBody(file)
                .flatMapObservable(fileBody -> Observable.<Progressible<File>>create(emitter -> {

                    long maxSize = file.getSize();

                    InputStream is = fileBody.byteStream();

                    try {

                        FileUtil.writeFile(is, target, maxSize,
                                written -> emitter.onNext(
                                        new Progressible<>(null, maxSize, written, file.getName(), false)
                                )
                        );

                        if (!target.setLastModified(file.getLastModified())){
                            MyLog.majorException(new IOException("Can't set last modified: " + target.getPath()));
                        }

                        emitter.onNext(new Progressible<>(target, maxSize, maxSize, file.getName(), true));

                        emitter.onComplete();

                    } catch (Exception e) {

                        if (!finished.get()) {
                            emitter.onError(e);
                        }

                    } finally {

                        IOUtil.closeQuietly(is);

                    }

                }))
                .doFinally(() -> finished.set(true));

        return Observable.concat(
                Observable.just(new Progressible<>(null, 0, 0, file.getName(), false)),
                request
        );
    }

    private List<AuroraFile> sortFiles(List<AuroraFile> files) {
        Collections.sort(files, FileUtil.AURORA_FILE_COMPARATOR);
        return files;
    }

    private void checkFilesHasOneType(List<AuroraFile> files) {

        boolean allSame = Stream.of(files)
                .map(AuroraFile::getType)
                .distinct()
                .count() == 1;

        if (!allSame) {
            throw new IllegalArgumentException("All files must have same type.");
        }

    }

    private void checkFilesType(
            String type, List<AuroraFile> files) throws IllegalArgumentException {

        boolean allInType = Stream.of(files)
                .allMatch(file -> type.equals(file.getType()));

        if (!allInType){
            throw new IllegalArgumentException("All files must be in one type.");
        }

    }

    public static class Factory {

        private SharedObservableStore cache;
        private Context appContext;
        private FilesLocalService localService;
        private File cacheDir;
        private File offlineDir;
        private File downloadsDir;
        private FilesMapperFactory mapperFactory;
        private SessionManager sessionManager;

        @Inject
        public Factory(SharedObservableStore cache,
                       Context appContext,
                       FilesLocalService localService,
                       @Named(CACHE_DIR) File cacheDir,
                       @Named(OFFLINE_DIR) File offlineDir,
                       @Named(DOWNLOADS_DIR) File downloadsDir,
                       FilesMapperFactory mapperFactory,
                       SessionManager sessionManager) {
            this.cache = cache;
            this.appContext = appContext;
            this.localService = localService;
            this.cacheDir = cacheDir;
            this.offlineDir = offlineDir;
            this.downloadsDir = downloadsDir;
            this.mapperFactory = mapperFactory;
            this.sessionManager = sessionManager;
        }

        public FileRepositoryImpl create(FileSubRepository fileSubRepo) {
            return new FileRepositoryImpl(
                    cache, appContext, fileSubRepo, localService, cacheDir, offlineDir,
                    downloadsDir, mapperFactory, sessionManager
            );
        }

    }

}
