package com.zamcan.madrassa.domain.auth.parent;

import com.zamcan.madrassa.data.model.AccountCredential;
import com.zamcan.madrassa.data.model.AccountRole;
import com.zamcan.madrassa.data.model.Parent;
import com.zamcan.madrassa.domain.auth.CredentialPolicy;
import com.zamcan.madrassa.domain.authorization.parent.ParentIdentityResolver;
import com.zamcan.madrassa.domain.authorization.parent.ParentIdentityResolverImpl;
import com.zamcan.madrassa.domain.repository.CredentialStore;
import com.zamcan.madrassa.domain.repository.ParentStore;
import com.zamcan.madrassa.domain.auth.PasswordVerifier;

public final class ParentAuthenticationService {

    private final ParentIdentityResolver identityResolver;
    private final CredentialStore credentialStore;
    private final ParentStore parentStore;
    private final PasswordVerifier passwordVerifier;

    public ParentAuthenticationService(
            ParentIdentityResolver identityResolver,
            CredentialStore credentialStore,
            ParentStore parentStore,
            PasswordVerifier passwordVerifier
    ) {
        if (identityResolver == null
                || credentialStore == null
                || parentStore == null
                || passwordVerifier == null) {
            throw new IllegalArgumentException(
                    "Parent authentication dependencies are required"
            );
        }

        this.identityResolver = identityResolver;
        this.credentialStore = credentialStore;
        this.parentStore = parentStore;
        this.passwordVerifier = passwordVerifier;
    }

    public ParentAuthenticationResult authenticate(
            String phone,
            String password
    ) {
        if (phone == null
                || phone.trim().isEmpty()
                || password == null
                || password.isEmpty()) {
            return ParentAuthenticationResult.invalidCredentials();
        }

        Parent parent = identityResolver.findByPhone(phone);

        if (parent == null) {
            return ParentAuthenticationResult.invalidCredentials();
        }

        if (!ParentIdentityResolverImpl.normalizePhone(phone)
                .equals(ParentIdentityResolverImpl.normalizePhone(parent.phone))) {
            return ParentAuthenticationResult.invalidCredentials();
        }

        if (!parent.active) {
            return ParentAuthenticationResult.accountInactive();
        }

        AccountCredential credential =
                credentialStore.findByAccount(
                        parent.id,
                        AccountRole.PARENT
                );

        if (!CredentialPolicy.canUse(credential)
                || !CredentialPolicy.belongsToMadrassa(
                        credential,
                        credential.madrassaId
                )) {
            return ParentAuthenticationResult.invalidCredentials();
        }

        if (!parent.id.equals(credential.accountId)) {
            return ParentAuthenticationResult.invalidCredentials();
        }

        if (!passwordVerifier.matches(
                password,
                credential.passwordHash,
                credential.passwordSalt
        )) {
            return ParentAuthenticationResult.invalidCredentials();
        }

        if (credential.firstLogin) {
            return ParentAuthenticationResult.passwordChangeRequired(parent);
        }

        credential.lastLoginAt = System.currentTimeMillis();
        credential.updatedAt = credential.lastLoginAt;
        credentialStore.update(credential);

        return ParentAuthenticationResult.success(parent);
    }
}
