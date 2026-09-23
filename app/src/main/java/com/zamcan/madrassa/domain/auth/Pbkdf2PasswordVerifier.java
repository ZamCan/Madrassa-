package com.zamcan.madrassa.domain.auth;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class Pbkdf2PasswordVerifier
        implements PasswordVerifier {

    private static final String ALGORITHM =
            "PBKDF2WithHmacSHA256";

    private static final int ITERATIONS =
            210_000;

    private static final int KEY_LENGTH_BITS =
            256;

    private static final int SALT_LENGTH_BYTES =
            16;

    private final SecureRandom secureRandom;

    public Pbkdf2PasswordVerifier() {
        this.secureRandom =
                new SecureRandom();
    }

    @Override
    public boolean matches(
            String rawPassword,
            String passwordHash,
            String passwordSalt
    ) {
        if (rawPassword == null ||
                passwordHash == null ||
                passwordSalt == null) {
            return false;
        }

        if (rawPassword.isEmpty() ||
                passwordHash.trim().isEmpty() ||
                passwordSalt.trim().isEmpty()) {
            return false;
        }

        try {
            byte[] salt =
                    Base64.getDecoder().decode(
                            passwordSalt
                    );

            byte[] expected =
                    Base64.getDecoder().decode(
                            passwordHash
                    );

            byte[] actual =
                    derive(
                            rawPassword,
                            salt
                    );

            return MessageDigest.isEqual(
                    expected,
                    actual
            );

        } catch (IllegalArgumentException |
                 GeneralSecurityException exception) {

            return false;
        }
    }

    @Override
    public PasswordHashResult hash(
            String rawPassword
    ) {
        if (rawPassword == null ||
                rawPassword.isEmpty()) {
            throw new IllegalArgumentException(
                    "Password must not be empty."
            );
        }

        byte[] salt =
                new byte[SALT_LENGTH_BYTES];

        secureRandom.nextBytes(salt);

        try {
            byte[] derived =
                    derive(
                            rawPassword,
                            salt
                    );

            return new PasswordHashResult(
                    Base64.getEncoder().encodeToString(
                            derived
                    ),
                    Base64.getEncoder().encodeToString(
                            salt
                    )
            );

        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException(
                    "Unable to hash password.",
                    exception
            );
        }
    }

    private byte[] derive(
            String rawPassword,
            byte[] salt
    ) throws GeneralSecurityException {

        PBEKeySpec specification =
                new PBEKeySpec(
                        rawPassword.toCharArray(),
                        salt,
                        ITERATIONS,
                        KEY_LENGTH_BITS
                );

        try {
            SecretKeyFactory factory =
                    SecretKeyFactory.getInstance(
                            ALGORITHM
                    );

            return factory
                    .generateSecret(specification)
                    .getEncoded();

        } finally {
            specification.clearPassword();
        }
    }
}
