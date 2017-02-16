package com.afterlogic.aurora.drive.presentation.modulesBackground.fileListener.assembly;

import android.support.annotation.NonNull;

import com.afterlogic.aurora.drive.presentation.assembly.assemblies.AssembliesAssemblyComponent;
import com.afterlogic.aurora.drive.presentation.common.modules.assembly.BaseInjector;
import com.afterlogic.aurora.drive.presentation.common.modules.assembly.Injector;
import com.afterlogic.aurora.drive.presentation.modulesBackground.fileListener.view.FileObserverService;
import com.afterlogic.aurora.drive.presentation.modulesBackground.fileListener.view.FileObserverView;

import javax.inject.Inject;

/**
 * Created by sashka on 16.02.17.<p/>
 * mail: sunnyday.development@gmail.com
 */

public class FileObserverInjector extends BaseInjector<FileObserverService, FileObserverView, FileObserverModule> implements Injector<FileObserverService> {

    @Inject FileObserverInjector(AssembliesAssemblyComponent component) {
        super(component);
    }

    @NonNull
    @Override
    protected FileObserverModule createModule() {
        return new FileObserverModule();
    }
}