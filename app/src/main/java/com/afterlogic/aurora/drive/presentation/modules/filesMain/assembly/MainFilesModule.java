package com.afterlogic.aurora.drive.presentation.modules.filesMain.assembly;

import com.afterlogic.aurora.drive.core.common.annotation.scopes.ModuleScope;
import com.afterlogic.aurora.drive.presentation.common.modules.assembly.PresentationModule;
import com.afterlogic.aurora.drive.presentation.modules.filesMain.interactor.MainFilesInteractor;
import com.afterlogic.aurora.drive.presentation.modules.filesMain.interactor.MainFilesInteractorImpl;
import com.afterlogic.aurora.drive.presentation.modules.filesMain.presenter.MainFilesPresenter;
import com.afterlogic.aurora.drive.presentation.modules.filesMain.presenter.MainFilesPresenterImpl;
import com.afterlogic.aurora.drive.presentation.modules.filesMain.router.MainFilesRouter;
import com.afterlogic.aurora.drive.presentation.modules.filesMain.router.MainFilesRouterImpl;
import com.afterlogic.aurora.drive.presentation.modules.filesMain.view.MainFilesView;
import com.afterlogic.aurora.drive.presentation.modules.filesMain.viewModel.MainFilesModel;
import com.afterlogic.aurora.drive.presentation.modules.filesMain.viewModel.MainFilesViewModel;

import dagger.Module;
import dagger.Provides;

/**
 * Created by sashka on 03.02.17.<p/>
 * mail: sunnyday.development@gmail.com
 */
@Module
public class MainFilesModule extends PresentationModule<MainFilesView> {

    @Provides @ModuleScope
    MainFilesPresenter presenter(MainFilesPresenterImpl presenter){
        return presenter;
    }

    @Provides @ModuleScope
    MainFilesViewModel viewModel(){
        return new MainFilesViewModel();
    }

    @Provides
    MainFilesInteractor interactor(MainFilesInteractorImpl interactor){
        return interactor;
    }

    @Provides
    MainFilesModel model(MainFilesViewModel model){
        return model.getController();
    }

    @Provides
    MainFilesRouter router(MainFilesRouterImpl router){
        return router;
    }

}
