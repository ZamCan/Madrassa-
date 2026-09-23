package com.zamcan.madrassa.domain.auth;

import com.zamcan.madrassa.data.model.AccountCredential;
import com.zamcan.madrassa.data.model.Madrassa;
import com.zamcan.madrassa.data.model.Ustadh;

public final class AuthenticationResult {

    public enum Status {
        SUCCESS,
        PASSWORD_CHANGE_REQUIRED,
        INVALID_CREDENTIALS,
        ACCOUNT_INACTIVE,
        MADRASSA_INACTIVE,
        FAILED
    }

    public final Status status;
    public final AccountCredential credential;
    public final Madrassa madrassa;
    public final Ustadh ustadh;

    private AuthenticationResult(
            Status status,
            AccountCredential credential,
            Madrassa madrassa,
            Ustadh ustadh
    ) {
        this.status = status;
        this.credential = credential;
        this.madrassa = madrassa;
        this.ustadh = ustadh;
    }

    public static AuthenticationResult success(
            AccountCredential credential,
            Madrassa madrassa,
            Ustadh ustadh
    ) {
        return new AuthenticationResult(
                Status.SUCCESS,
                credential,
                madrassa,
                ustadh
        );
    }

    public static AuthenticationResult passwordChangeRequired(
            AccountCredential credential,
            Madrassa madrassa,
            Ustadh ustadh
    ) {
        return new AuthenticationResult(
                Status.PASSWORD_CHANGE_REQUIRED,
                credential,
                madrassa,
                ustadh
        );
    }

    public static AuthenticationResult invalidCredentials() {
        return new AuthenticationResult(
                Status.INVALID_CREDENTIALS,
                null,
                null,
                null
        );
    }

    public static AuthenticationResult accountInactive() {
        return new AuthenticationResult(
                Status.ACCOUNT_INACTIVE,
                null,
                null,
                null
        );
    }

    public static AuthenticationResult madrassaInactive() {
        return new AuthenticationResult(
                Status.MADRASSA_INACTIVE,
                null,
                null,
                null
        );
    }

    public static AuthenticationResult failed() {
        return new AuthenticationResult(
                Status.FAILED,
                null,
                null,
                null
        );
    }
}
