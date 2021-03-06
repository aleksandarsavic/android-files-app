package com.afterlogic.aurora.drive.data.modules.auth;

import com.afterlogic.aurora.drive.core.common.annotation.scopes.DataScope;
import com.afterlogic.aurora.drive.core.consts.Const;
import com.afterlogic.aurora.drive.model.AuthorizedAuroraSession;
import com.afterlogic.aurora.drive.model.error.UnknownApiVersionError;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * Created by aleksandrcikin on 25.08.17.
 * mail: mail@sunnydaydev.me
 */

@DataScope
public class AuthenticatorService {

    private final AuthenticatorSubService p7AuthenticatorSubService;
    private final AuthenticatorSubService p8AuthenticatorSubService;

    @Inject
    AuthenticatorService(P7AuthenticatorSubService p7AuthenticatorService,
                         P8AuthenticatorSubService p8AuthenticatorService) {

        this.p7AuthenticatorSubService = p7AuthenticatorService;
        this.p8AuthenticatorSubService = p8AuthenticatorService;
    }

    public Completable logout() {
        return Completable.complete();
    }

    public Single<AuthorizedAuroraSession> login(String host, String login, String password) {
        return getAuthenticatorService(host)
                .flatMap(service -> service.login(host, login, password));
    }

    public Single<AuthorizedAuroraSession> createSession(String host, String token) {
        return getAuthenticatorService(host)
                .flatMap(service -> service.byToken(host, token));
    }

    public Single<Integer> getApiVersion(String host) {
        return Observable.concat(
                p8AuthenticatorSubService.getApiVersion(host)
                        .onErrorComplete() // TODO: more effective check
                        .toObservable(),
                p7AuthenticatorSubService.getApiVersion(host)
                        .onErrorComplete() // TODO: more effective check
                        .toObservable()
        )//--->
                .switchIfEmpty(Observable.error(new UnknownApiVersionError()))
                .firstOrError();
    }

    public Single<Boolean> checkExternalLoginFormsEnabled(String host) {
        return getAuthenticatorService(host)
                .flatMap(service -> service.isExternalClientLoginFormsAvailable(host));
    }

    private Single<AuthenticatorSubService> getAuthenticatorService(String host) {
        return getApiVersion(host)
                .map(version -> version == Const.ApiVersion.API_P7
                        ? p7AuthenticatorSubService : p8AuthenticatorSubService);
    }


}
