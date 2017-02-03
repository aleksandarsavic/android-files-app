package com.afterlogic.aurora.drive.data.modules.apiChecker.p7.repository;

import com.afterlogic.aurora.drive.data.common.annotations.RepositoryCache;
import com.afterlogic.aurora.drive.data.common.cache.SharedObservableStore;
import com.afterlogic.aurora.drive.data.modules.apiChecker.ApiCheckRepository;
import com.afterlogic.aurora.drive.data.common.repository.Repository;
import com.afterlogic.aurora.drive.data.modules.apiChecker.p7.service.ApiCheckerServiceP7;

import javax.inject.Inject;

import io.reactivex.Single;
import okhttp3.HttpUrl;

/**
 * Created by sashka on 19.10.16.<p/>
 * mail: sunnyday.development@gmail.com
 */
public class ApiCheckRepository7Impl extends Repository implements ApiCheckRepository {

    private static final String CHECK_P_7 = "checkP7";
    private final ApiCheckerServiceP7 mAuthService;

    @SuppressWarnings("WeakerAccess")
    @Inject public ApiCheckRepository7Impl(@RepositoryCache SharedObservableStore cache, ApiCheckerServiceP7 authService) {
        super(cache, CHECK_P_7);
        mAuthService = authService;
    }

    @Override
    public Single<Boolean> checkDomain(HttpUrl domain) {
        return mAuthService.getSystemAppData(domain).map(result -> true);
    }
}
