package com.afterlogic.aurora.drive.presentation.modules.filelist.router;

import com.afterlogic.aurora.drive.model.AuroraFile;

import java.io.File;
import java.util.List;

/**
 * Created by sashka on 07.02.17.<p/>
 * mail: sunnyday.development@gmail.com
 */

public interface FileListRouter {

    int FILE_SELECT_CODE = 1;

    void openImagePreview(AuroraFile target, List<AuroraFile> dirContent);

    void openLink(AuroraFile target);

    boolean canOpenFile(AuroraFile file);

    void openFile(AuroraFile remote, File file);

    void openSendTo(AuroraFile source, File file);

    void openUploadFileChooser();
}
