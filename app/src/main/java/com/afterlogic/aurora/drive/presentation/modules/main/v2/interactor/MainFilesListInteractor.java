package com.afterlogic.aurora.drive.presentation.modules.main.v2.interactor;

import android.content.Context;
import android.net.Uri;
import android.os.PowerManager;

import com.afterlogic.aurora.drive.core.common.util.FileUtil;
import com.afterlogic.aurora.drive.core.common.util.Holder;
import com.afterlogic.aurora.drive.data.modules.files.repository.FilesRepository;
import com.afterlogic.aurora.drive.model.AuroraFile;
import com.afterlogic.aurora.drive.model.Progressible;
import com.afterlogic.aurora.drive.presentation.modules._baseFiles.v2.interactor.SearchableFilesListInteractor;
import com.afterlogic.aurora.drive.presentation.modulesBackground.sync.view.SyncListener;
import com.afterlogic.aurora.drive.presentation.modulesBackground.sync.viewModel.SyncProgress;

import java.io.File;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;

import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

import static com.afterlogic.aurora.drive.data.modules.files.FilesDataModule.CACHE_DIR;

/**
 * Created by aleksandrcikin on 11.07.17.
 * mail: mail@sunnydaydev.me
 */

public class MainFilesListInteractor extends SearchableFilesListInteractor {

    private static int sWakeLockId = 0;

    private final FilesRepository filesRepository;
    private final SyncListener syncListener;

    private final Context appContext;
    private final File cacheDir;

    private final MainFilesListViewInteractor viewInteractor;

    private volatile int syncObservablesCount = 0;

    @Inject
    MainFilesListInteractor(FilesRepository filesRepository,
                            Context appContext,
                            @Named(CACHE_DIR) File cacheDir,
                            MainFilesListViewInteractor viewInteractor) {
        super(filesRepository);
        this.filesRepository = filesRepository;
        this.syncListener = new SyncListener(appContext);
        this.appContext = appContext;
        this.cacheDir = cacheDir;
        this.viewInteractor = viewInteractor;
    }

    public Single<Uri> getThumbnail(AuroraFile file) {
        return filesRepository.getFileThumbnail(file);
    }

    public Single<Boolean> getOfflineStatus(AuroraFile file) {
        return filesRepository.getOfflineStatus(file);
    }

    public Observable<SyncProgress> getSyncProgress() {
        return syncListener.getProgressSource()
                .doOnSubscribe(disposable -> {
                    if (syncObservablesCount == 0) {
                        syncListener.onStart();
                    }
                    syncObservablesCount++;
                })
                .doFinally(() -> {
                    syncObservablesCount--;
                    if (syncObservablesCount == 0) {
                        syncListener.onStop();
                    }
                });
    }

    public Observable<Progressible<File>> downloadForOpen(AuroraFile file) {
        File target = FileUtil.getFile(cacheDir, file);
        return filesRepository.downloadOrGetOffline(file, target)
                .startWith(Observable.just(new Progressible<>(null, 0, 0, file.getName(), false)))
                .compose(this::prepareLoadTask);
    }

    public Maybe<String> getNewFolderName() {
        return viewInteractor.getNewFolderName();
    }

    public Single<AuroraFile> createNewFolder(String name, AuroraFile parentFolder) {
        return Single.defer(() -> {
            AuroraFile newFolder = AuroraFile.create(parentFolder, name, true);

            return filesRepository.createFolder(newFolder)
                    .andThen(Single.just(newFolder));
        });
    }

    private <T> Observable<T> prepareLoadTask(Observable<T> upstream) {
        return upstream.startWith(
                viewInteractor.requireWritePermission()
                        .observeOn(Schedulers.io())
                        .toObservable()
        )//-----|
                .compose(this::wakeLock);

    }

    private   <T> Observable<T> wakeLock(Observable<T> observable){
        Holder<PowerManager.WakeLock> wakeLockHolder = new Holder<>();
        return observable
                .doOnSubscribe(disposable -> {
                    PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "loadTask:" + sWakeLockId++);
                    wakeLockHolder.set(wakeLock);
                    wakeLock.acquire(TimeUnit.MINUTES.toMillis(30));
                })
                .doFinally(() -> wakeLockHolder.get().release());
    }
}
