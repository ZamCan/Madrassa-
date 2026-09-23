package com.zamcan.madrassa.domain.auth.parent;

import com.zamcan.madrassa.data.model.AccountCredential;
import com.zamcan.madrassa.data.model.AccountRole;
import com.zamcan.madrassa.data.model.Parent;
import com.zamcan.madrassa.domain.auth.CredentialPolicy;
import com.zamcan.madrassa.domain.common.IdGenerator;
import com.zamcan.madrassa.domain.repository.ParentProvisioningStore;
import com.zamcan.madrassa.domain.repository.ParentStore;
import com.zamcan.madrassa.domain.auth.PasswordVerifier;

public final class ParentProvisioningService {

    public enum Status {
        SUCCESS,
        INVALID_PARENT,
        INVALID_PASSWORD,
        DUPLICATE_PHONE,
        FAILED
    }

    public static final class Result {

        public final Status status;
        public final Parent parent;

        private Result(
                Status status,
                Parent parent
        ) {
            this.status = status;
            this.parent = parent;
        }

        public static Result success(Parent parent) {
            return new Result(Status.SUCCESS, parent);
        }

        public static Result invalidParent() {
            return new Result(
                    Status.INVALID_PARENT,
                    null
            );
        }

        public static Result invalidPassword() {
            return new Result(
                    Status.INVALID_PASSWORD,
                    null
            );
        }

        public static Result duplicatePhone() {
            return new Result(
                    Status.DUPLICATE_PHONE,
                    null
            );
        }

        public static Result failed() {
            return new Result(
                    Status.FAILED,
                    null
            );
        }
    }

    private final ParentStore parentStore;
    private final ParentProvisioningStore provisioningStore;
    private final PasswordVerifier passwordVerifier;

    public ParentProvisioningService(
            ParentStore parentStore,
            ParentProvisioningStore provisioningStore,
            PasswordVerifier passwordVerifier
    ) {
        this.parentStore = parentStore;
        this.provisioningStore = provisioningStore;
        this.passwordVerifier = passwordVerifier;
    }

    public Result provision(
            String madrassaId,
            String fullName,
            String phone,
            String initialPassword
    ) {
        if (blank(madrassaId)
                || blank(fullName)
                || blank(phone)) {
            return Result.invalidParent();
        }

        if (!CredentialPolicy.validNewPassword(
                AccountRole.PARENT,
                initialPassword
        )) {
            return Result.invalidPassword();
        }

        String normalizedPhone =
                normalizePhone(phone);

        if (normalizedPhone == null) {
            return Result.invalidParent();
        }

        if (parentStore.existsByPhone(
                normalizedPhone
        )) {
            return Result.duplicatePhone();
        }

        Parent parent = new Parent();

        parent.id = IdGenerator.newId();
        parent.madrassaId = madrassaId.trim();
        parent.fullName = fullName.trim();
        parent.phone = normalizedPhone;
        parent.firstLogin = true;
        parent.active = true;

        PasswordVerifier.PasswordHashResult
                hash = passwordVerifier.hash(
                        initialPassword
                );

        long now = System.currentTimeMillis();

        AccountCredential credential =
                new AccountCredential();

        credential.id = IdGenerator.newId();
        credential.accountId = parent.id;
        credential.role = AccountRole.PARENT;
        credential.madrassaId = parent.madrassaId;
        credential.passwordHash = hash.hash;
        credential.passwordSalt = hash.salt;
        credential.active = true;
        credential.firstLogin = true;
        credential.createdAt = now;
        credential.updatedAt = now;
        credential.lastLoginAt = 0L;

        try {
            provisioningStore.provision(
                    parent,
                    credential
            );

            return Result.success(parent);

        } catch (RuntimeException e) {
            return Result.failed();
        }
    }

    public static String normalizePhone(String phone) {
        if (phone == null) {
            return null;
        }

        String value = phone.trim();

        if (value.isEmpty()) {
            return null;
        }

        StringBuilder result =
                new StringBuilder();

        for (char c : value.toCharArray()) {

            if (Character.isDigit(c)) {
                result.append(c);

            } else if (c == '+'
                    || c == ' '
                    || c == '-'
                    || c == '('
                    || c == ')'
                    || c == '.') {

                if (c == '+') {
                    if (result.length() == 0) {
                        result.append(c);
                    } else {
                        return null;
                    }
                }

            } else {
                return null;
            }
        }

        if (result.length() == 0) {
            return null;
        }

        String normalized =
                result.toString();

        if (normalized.startsWith("00")) {
            normalized =
                    "+" + normalized.substring(2);
        }

        return normalized;
    }

    private static boolean blank(String value) {
        return value == null
                || value.trim().isEmpty();
    }
}
