package com.afterlogic.aurora.drive.application.navigation;

import android.app.Activity;

import com.afterlogic.aurora.drive.core.common.interfaces.Consumer;

import ru.terrakok.cicerone.Router;

/**
 * Created by aleksandrcikin on 11.07.17.
 * mail: mail@sunnydaydev.me
 */

public class AppRouter extends Router {

    public static final String MAIN = "main";
    public static final String REPLACE = "replace";
    public static final String COPY = "copy";
    public static final String LOGIN = "login";
    public static final String OFFLINE = "offline";
    public static final String ABOUT = "about";
    public static final String IMAGE_VIEW = "imageView";

    public static final String MAIN_FILE_ACTIONS = "mainFileActions";

    public static final String EXTERNAL_BROWSER = "externalBrowser";
    public static final String EXTERNAL_OPEN_FILE = "externalOpenFile";
    public static final String EXTERNAL_CHOOSE_FILE_FOR_UPLOAD = "externalChooseFileForUpload";
    public static final String EXTERNAL_SHARE = "externalShare";

    public static final int RESULT_CODE_REPLACE = 1000;

    public void navigateToWithResult(String screenKey, int requestId) {
        navigateToWithResult(screenKey, requestId, null);
    }

    public void navigateToWithResult(String screenKey, int requestId, Object data) {
        executeCommands(new ForwardWithResult(screenKey, requestId, data));
    }

    public void navigateTo(String screenKey, Object data, Consumer<Throwable> onError) {
        executeCommands(new ForwardWithErrorHandling(screenKey, onError, data));
    }

    public void navigateTo(String screenKey, Consumer<Throwable> onError) {
        executeCommands(new ForwardWithErrorHandling(screenKey, onError, null));
    }
}
