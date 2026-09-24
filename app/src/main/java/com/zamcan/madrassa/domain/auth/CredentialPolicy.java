package com.zamcan.madrassa.domain.auth;

import com.zamcan.madrassa.data.model.AccountCredential;
import com.zamcan.madrassa.data.model.AccountRole;
import com.zamcan.madrassa.core.validation.EduNoorRules;

public final class CredentialPolicy {

    private CredentialPolicy() {
    }

    public static boolean canUse(
            AccountCredential credential
    ) {
        if (credential == null) {
            return false;
        }

        if (!credential.active) {
            return false;
        }

        if (credential.accountId == null ||
                credential.accountId.trim().isEmpty()) {
            return false;
        }

        if (credential.role == null) {
            return false;
        }

        return true;
    }

    public static boolean validNewPassword(
            AccountRole role,
            String password
    ) {
        if (role == AccountRole.PARENT) {
            return EduNoorRules.validParentPassword(
                    password
            );
        }

        if (role == AccountRole.USTADH ||
                role == AccountRole.ADMIN) {
            return EduNoorRules.validUstadhPassword(
                    password
            );
        }

        return false;
    }

    public static boolean requiresInitialPasswordChange(
            AccountCredential credential
    ) {
        return credential != null &&
                credential.firstLogin;
    }

    public static boolean belongsToMadrassa(
            AccountCredential credential,
            String madrassaId
    ) {
        if (!canUse(credential) ||
                madrassaId == null ||
                madrassaId.trim().isEmpty() ||
                credential.madrassaId == null ||
                credential.madrassaId.trim().isEmpty()) {
            return false;
        }

        return madrassaId.trim().equals(
                credential.madrassaId.trim()
        );
    }
}
