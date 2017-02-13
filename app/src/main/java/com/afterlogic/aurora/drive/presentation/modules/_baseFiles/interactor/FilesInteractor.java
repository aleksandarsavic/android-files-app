package com.afterlogic.aurora.drive.presentation.modules._baseFiles.interactor;

import com.afterlogic.aurora.drive.presentation.common.modules.interactor.Interactor;
import com.afterlogic.aurora.drive.model.FileType;

import java.util.List;

import io.reactivex.Single;

/**
 * Created by sashka on 10.02.17.<p/>
 * mail: sunnyday.development@gmail.com
 */

public interface FilesInteractor extends Interactor {

    Single<List<FileType>> getAvailableFileTypes();
}