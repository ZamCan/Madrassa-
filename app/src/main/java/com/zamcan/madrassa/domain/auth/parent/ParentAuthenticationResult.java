package com.zamcan.madrassa.domain.auth.parent;

import com.zamcan.madrassa.data.model.Parent;

public final class ParentAuthenticationResult {

    public enum Status {
        SUCCESS,
        PASSWORD_CHANGE_REQUIRED,
        INVALID_CREDENTIALS,
        ACCOUNT_INACTIVE,
        FAILED
    }

    public final Status status;
    public final Parent parent;

    private ParentAuthenticationResult(
            Status status,
            Parent parent
    ) {
        this.status = status;
        this.parent = parent;
    }

    public static ParentAuthenticationResult success(Parent parent) {
        return new ParentAuthenticationResult(Status.SUCCESS, parent);
    }

    public static ParentAuthenticationResult passwordChangeRequired(
            Parent parent
    ) {
        return new ParentAuthenticationResult(
                Status.PASSWORD_CHANGE_REQUIRED,
                parent
        );
    }

    public static ParentAuthenticationResult invalidCredentials() {
        return new ParentAuthenticationResult(
                Status.INVALID_CREDENTIALS,
                null
        );
    }

    public static ParentAuthenticationResult accountInactive() {
        return new ParentAuthenticationResult(
                Status.ACCOUNT_INACTIVE,
                null
        );
    }

    public static ParentAuthenticationResult failed() {
        return new ParentAuthenticationResult(
                Status.FAILED,
                null
        );
    }

    public boolean isSuccess() {
        return status == Status.SUCCESS
                || status == Status.PASSWORD_CHANGE_REQUIRED;
    }
}
