package com.zamcan.madrassa.domain.auth;

import com.zamcan.madrassa.data.model.AccountCredential;
import com.zamcan.madrassa.data.model.AccountRole;
import com.zamcan.madrassa.data.model.ApprovalStatus;
import com.zamcan.madrassa.data.model.Madrassa;
import com.zamcan.madrassa.data.model.Ustadh;
import com.zamcan.madrassa.domain.authorization.AccountIdentityResolver;
import com.zamcan.madrassa.domain.repository.CredentialStore;
import com.zamcan.madrassa.domain.repository.MadrassaStore;
import com.zamcan.madrassa.domain.repository.UstadhStore;

public final class UstadhAuthenticationService {

    private final AccountIdentityResolver identityResolver;
    private final CredentialStore credentialStore;
    private final MadrassaStore madrassaStore;
    private final UstadhStore ustadhStore;
    private final PasswordVerifier passwordVerifier;

    public UstadhAuthenticationService(
            AccountIdentityResolver identityResolver,
            CredentialStore credentialStore,
            MadrassaStore madrassaStore,
            UstadhStore ustadhStore,
            PasswordVerifier passwordVerifier
    ) {
        if (identityResolver == null ||
                credentialStore == null ||
                madrassaStore == null ||
                ustadhStore == null ||
                passwordVerifier == null) {
            throw new IllegalArgumentException(
                    "Authentication dependencies must not be null."
            );
        }

        this.identityResolver =
                identityResolver;

        this.credentialStore =
                credentialStore;

        this.madrassaStore =
                madrassaStore;

        this.ustadhStore =
                ustadhStore;

        this.passwordVerifier =
                passwordVerifier;
    }

    public AuthenticationResult authenticate(
            String identifier,
            String password
    ) {
        if (isBlank(identifier) ||
                password == null ||
                password.isEmpty()) {
            return AuthenticationResult
                    .invalidCredentials();
        }

        AccountIdentityResolver.AccountIdentity identity =
                identityResolver.resolve(
                        identifier,
                        AccountRole.USTADH
                );

        if (identity == null ||
                isBlank(identity.accountId) ||
                isBlank(identity.madrassaId)) {
            return AuthenticationResult
                    .invalidCredentials();
        }

        AccountCredential credential =
                credentialStore.findByAccount(
                        identity.accountId,
                        AccountRole.USTADH
                );

        if (credential == null ||
                !CredentialPolicy.canUse(credential) ||
                !CredentialPolicy.belongsToMadrassa(
                        credential,
                        identity.madrassaId
                )) {
            return AuthenticationResult
                    .invalidCredentials();
        }

        Ustadh ustadh =
                ustadhStore.findById(
                        identity.accountId
                );

        if (ustadh == null ||
                !ustadh.active ||
                !com.zamcan.madrassa.domain.authorization
                        .UstadhAccessPolicy.belongsToMadrassa(
                        ustadh,
                        identity.madrassaId
                )) {
            return AuthenticationResult
                    .accountInactive();
        }

        Madrassa madrassa =
                madrassaStore.findById(
                        identity.madrassaId
                );

        if (madrassa == null) {
            return AuthenticationResult
                    .invalidCredentials();
        }

        if (!isMadrassaActive(madrassa)) {
            return AuthenticationResult
                    .madrassaInactive();
        }

        boolean passwordMatches =
                passwordVerifier.matches(
                        password,
                        credential.passwordHash,
                        credential.passwordSalt
                );

        if (!passwordMatches) {
            return AuthenticationResult
                    .invalidCredentials();
        }

        if (CredentialPolicy
                .requiresInitialPasswordChange(
                        credential
                )) {
            return AuthenticationResult
                    .passwordChangeRequired(
                            credential,
                            madrassa,
                            ustadh
                    );
        }

        return AuthenticationResult.success(
                credential,
                madrassa,
                ustadh
        );
    }

    private boolean isMadrassaActive(
            Madrassa madrassa
    ) {
        return madrassa.approvalStatus ==
                ApprovalStatus.ACTIVE;
    }

    private boolean isBlank(
            String value
    ) {
        return value == null ||
                value.trim().isEmpty();
    }
}
