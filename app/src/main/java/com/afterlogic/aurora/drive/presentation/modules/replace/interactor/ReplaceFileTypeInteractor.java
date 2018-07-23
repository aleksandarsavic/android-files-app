package com.afterlogic.aurora.drive.presentation.modules.replace.interactor;

import com.afterlogic.aurora.drive.data.modules.files.repository.FilesRepository;
import com.afterlogic.aurora.drive.model.AuroraFile;
import com.afterlogic.aurora.drive.presentation.modules.baseFiles.v2.interactor.SearchableFilesListInteractor;

import javax.inject.Inject;

import io.reactivex.Maybe;
import io.reactivex.Single;

/**
 * Created by aleksandrcikin on 04.07.17.
 * mail: mail@sunnydaydev.me
 */

public class ReplaceFileTypeInteractor extends SearchableFilesListInteractor {

    private final FilesRepository filesRepository;
    private final ReplaceViewInteractor viewInteractor;

    @Inject
    ReplaceFileTypeInteractor(FilesRepository filesRepository, ReplaceViewInteractor viewInteractor) {
        super(filesRepository);
        this.filesRepository = filesRepository;
        this.viewInteractor = viewInteractor;
    }

    public Maybe<String> getCreateFolderName() {
        return viewInteractor.getFolderName();
    }

    public Single<AuroraFile> createFolder(String name, AuroraFile currentFolder) {
        AuroraFile newFolder = AuroraFile.create(currentFolder, name, true);
        return filesRepository.createFolder(newFolder)
                .andThen(Single.just(newFolder));
    }
}
