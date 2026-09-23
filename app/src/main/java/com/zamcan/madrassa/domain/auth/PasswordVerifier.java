package com.zamcan.madrassa.domain.auth;

public interface PasswordVerifier {

    boolean matches(
            String rawPassword,
            String passwordHash,
            String passwordSalt
    );

    PasswordHashResult hash(
            String rawPassword
    );

    final class PasswordHashResult {

        public final String hash;
        public final String salt;

        public PasswordHashResult(
                String hash,
                String salt
        ) {
            this.hash = hash;
            this.salt = salt;
        }
    }
}
