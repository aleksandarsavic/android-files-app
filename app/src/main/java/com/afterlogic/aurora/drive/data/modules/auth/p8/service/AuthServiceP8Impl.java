package com.afterlogic.aurora.drive.data.modules.auth.p8.service;

import com.afterlogic.aurora.drive.data.common.annotations.P8;
import com.afterlogic.aurora.drive.data.common.network.ParamsBuilder;
import com.afterlogic.aurora.drive.data.common.network.p8.Api8;
import com.afterlogic.aurora.drive.data.common.network.p8.CloudServiceP8;
import com.afterlogic.aurora.drive.model.AuthToken;
import com.afterlogic.aurora.drive._unrefactored.model.project8.ApiResponseP8;
import com.google.gson.Gson;

import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Single;

/**
 * Created by sashka on 11.10.16.<p/>
 * mail: sunnyday.development@gmail.com
 */
public class AuthServiceP8Impl extends CloudServiceP8 implements AuthServiceP8{

    private final Api8 mApi;

    @SuppressWarnings("WeakerAccess")
    @Inject public AuthServiceP8Impl(Api8 api, @P8 Gson gson) {
        super(Api8.Module.CORE, gson);
        mApi = api;
    }

    @Override
    public Single<ApiResponseP8<AuthToken>> login(String login, String password) {
        Map<String, Object> params = new ParamsBuilder()
                .put(Api8.Param.LOGIN, login)
                .put(Api8.Param.PASSWORD, password)
                .put(Api8.Param.SIGN_ME, false)
                .create();

        Map<String, Object> fields = getDefaultFields(Api8.Method.LOGIN, params);
        return mApi.login(fields);
    }
}
