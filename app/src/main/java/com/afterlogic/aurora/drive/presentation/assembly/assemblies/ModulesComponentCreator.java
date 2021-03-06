package com.afterlogic.aurora.drive.presentation.assembly.assemblies;

import com.afterlogic.aurora.drive.presentation.assembly.MVVMComponentsStore;
import com.afterlogic.aurora.drive.presentation.common.modules.assembly.PresentationModulesStore;
import com.afterlogic.aurora.drive.presentation.modules.accountInfo.assembly.AccountInfoComponent;
import com.afterlogic.aurora.drive.presentation.modules.choise.assembly.ChoiseComponent;
import com.afterlogic.aurora.drive.presentation.modules.choise.assembly.ChoiseFilesComponent;
import com.afterlogic.aurora.drive.presentation.modules.choise.assembly.ChoiseFilesModule;
import com.afterlogic.aurora.drive.presentation.modules.choise.assembly.ChoiseModule;
import com.afterlogic.aurora.drive.presentation.modules.fileView.assembly.FileViewComponent;
import com.afterlogic.aurora.drive.presentation.modules.fileView.assembly.FileViewModule;
import com.afterlogic.aurora.drive.presentation.modules.start.assembly.StartComponent;
import com.afterlogic.aurora.drive.presentation.modules.start.assembly.StartModule;
import com.afterlogic.aurora.drive.presentation.modulesBackground.accountAction.AccountActionComponent;
import com.afterlogic.aurora.drive.presentation.modulesBackground.fileListener.assembly.FileObserverComponent;
import com.afterlogic.aurora.drive.presentation.modulesBackground.fileListener.assembly.FileObserverModule;
import com.afterlogic.aurora.drive.presentation.modulesBackground.sync.assembly.SyncComponent;
import com.afterlogic.aurora.drive.presentation.modulesBackground.sync.assembly.SyncModule;

import dagger.Subcomponent;

/**
 * Created by sashka on 31.08.16.<p/>
 * mail: sunnyday.development@gmail.com
 *
 * Presentation's dagger modules component creator.
 *
 * NOTE:
 * For default component creation method in BaseWireframe all methods must be called 'plus'.
 */
@SuppressWarnings("unused")
@Subcomponent(modules = AssembliesAssemblyModule.class)
public interface ModulesComponentCreator {

    @Deprecated
    PresentationModulesStore store();

    MVVMComponentsStore mvvmStore();

    StartComponent plus(StartModule module);

    ChoiseComponent plus(ChoiseModule module);
    ChoiseFilesComponent plus(ChoiseFilesModule module);

    SyncComponent plus(SyncModule module);

    FileObserverComponent plus(FileObserverModule module);

    FileViewComponent plus(FileViewModule module);

    AccountActionComponent accountActionReceiver();

    AccountInfoComponent accountInfo();
}
