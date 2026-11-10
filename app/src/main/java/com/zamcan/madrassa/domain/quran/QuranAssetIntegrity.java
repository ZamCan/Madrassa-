package com.zamcan.madrassa.domain.quran;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/**
 * Streams content through SHA-256 without loading large recitation files into RAM.
 * The expected digest must come from a trusted, versioned asset manifest.
 */
public final class QuranAssetIntegrity {
    private QuranAssetIntegrity() {}

    public static String sha256(InputStream source) throws IOException {
        if (source == null) throw new IllegalArgumentException("Missing asset stream");
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int n;
            while ((n = source.read(buffer)) != -1) {
                if (n > 0) digest.update(buffer, 0, n);
            }
            StringBuilder result = new StringBuilder(64);
            for (byte b : digest.digest()) {
                result.append(String.format(Locale.ROOT, "%02x", b & 0xff));
            }
            return result.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    public static void verify(InputStream source, String expectedSha256) throws IOException {
        if (expectedSha256 == null || !expectedSha256.matches("[0-9a-fA-F]{64}")) {
            throw new IllegalArgumentException("Missing or malformed trusted SHA-256");
        }
        String actual = sha256(source);
        if (!MessageDigest.isEqual(
                actual.getBytes(java.nio.charset.StandardCharsets.US_ASCII),
                expectedSha256.toLowerCase(Locale.ROOT)
                        .getBytes(java.nio.charset.StandardCharsets.US_ASCII))) {
            throw new SecurityException("Quran asset integrity verification failed");
        }
    }
}
