package com.zamcan.madrassa.domain.auth.parent;

import com.zamcan.madrassa.data.model.AccountCredential;
import com.zamcan.madrassa.data.model.AccountRole;
import com.zamcan.madrassa.data.model.Parent;
import com.zamcan.madrassa.domain.auth.CredentialPolicy;
import com.zamcan.madrassa.domain.repository.CredentialStore;
import com.zamcan.madrassa.domain.repository.ParentStore;
import com.zamcan.madrassa.domain.auth.PasswordVerifier;

public final class ParentPasswordService {

    public enum Status {
        SUCCESS,
        INVALID_ACCOUNT,
        INVALID_CURRENT_PASSWORD,
        INVALID_NEW_PASSWORD,
        FAILED
    }

    public static final class Result {
        public final Status status;

        private Result(Status status) {
            this.status = status;
        }

        public static Result success() {
            return new Result(Status.SUCCESS);
        }

        public static Result invalidAccount() {
            return new Result(Status.INVALID_ACCOUNT);
        }

        public static Result invalidCurrentPassword() {
            return new Result(Status.INVALID_CURRENT_PASSWORD);
        }

        public static Result invalidNewPassword() {
            return new Result(Status.INVALID_NEW_PASSWORD);
        }

        public static Result failed() {
            return new Result(Status.FAILED);
        }
    }

    private final ParentStore parentStore;
    private final CredentialStore credentialStore;
    private final PasswordVerifier passwordVerifier;

    public ParentPasswordService(
            ParentStore parentStore,
            CredentialStore credentialStore,
            PasswordVerifier passwordVerifier
    ) {
        this.parentStore = parentStore;
        this.credentialStore = credentialStore;
        this.passwordVerifier = passwordVerifier;
    }

    public Result changePassword(
            String parentId,
            String currentPassword,
            String newPassword
    ) {
        if (parentId == null || parentId.trim().isEmpty()) {
            return Result.invalidAccount();
        }

        Parent parent = parentStore.findById(parentId.trim());

        if (parent == null || !parent.active) {
            return Result.invalidAccount();
        }

        AccountCredential credential =
                credentialStore.findByAccount(
                        parent.id,
                        AccountRole.PARENT
                );

        if (!CredentialPolicy.canUse(credential)
                || !parent.id.equals(credential.accountId)) {
            return Result.invalidAccount();
        }

        if (currentPassword == null
                || !passwordVerifier.matches(
                        currentPassword,
                        credential.passwordHash,
                        credential.passwordSalt
                )) {
            return Result.invalidCurrentPassword();
        }

        if (!CredentialPolicy.validNewPassword(
                AccountRole.PARENT,
                newPassword
        )) {
            return Result.invalidNewPassword();
        }

        PasswordVerifier.PasswordHashResult
                result = passwordVerifier.hash(newPassword);

        credential.passwordHash = result.hash;
        credential.passwordSalt = result.salt;
        credential.firstLogin = false;
        credential.updatedAt = System.currentTimeMillis();

        credentialStore.update(credential);

        return Result.success();
    }

    public Result setInitialPassword(
            String parentId,
            String newPassword
    ) {
        if (parentId == null || parentId.trim().isEmpty()) {
            return Result.invalidAccount();
        }

        Parent parent = parentStore.findById(parentId.trim());

        if (parent == null || !parent.active) {
            return Result.invalidAccount();
        }

        AccountCredential credential =
                credentialStore.findByAccount(
                        parent.id,
                        AccountRole.PARENT
                );

        if (!CredentialPolicy.canUse(credential)
                || !credential.firstLogin) {
            return Result.invalidAccount();
        }

        if (!CredentialPolicy.validNewPassword(
                AccountRole.PARENT,
                newPassword
        )) {
            return Result.invalidNewPassword();
        }

        PasswordVerifier.PasswordHashResult
                result = passwordVerifier.hash(newPassword);

        credential.passwordHash = result.hash;
        credential.passwordSalt = result.salt;
        credential.firstLogin = false;
        credential.updatedAt = System.currentTimeMillis();

        credentialStore.update(credential);

        return Result.success();
    }
}
