package com.zamcan.madrassa.domain.common;

public final class TenantPolicy {

    private TenantPolicy() {
    }

    public static boolean sameMadrassa(
            String firstMadrassaId,
            String secondMadrassaId
    ) {
        if (isBlank(firstMadrassaId)
                || isBlank(secondMadrassaId)) {
            return false;
        }

        return firstMadrassaId.trim()
                .equals(secondMadrassaId.trim());
    }

    public static void requireSameMadrassa(
            String firstMadrassaId,
            String secondMadrassaId
    ) {
        if (!sameMadrassa(
                firstMadrassaId,
                secondMadrassaId
        )) {
            throw new SecurityException(
                    "Cross-Madrassa operation is forbidden."
            );
        }
    }

    public static void requireMadrassa(
            String actualMadrassaId,
            String expectedMadrassaId
    ) {
        if (!sameMadrassa(
                actualMadrassaId,
                expectedMadrassaId
        )) {
            throw new SecurityException(
                    "Record does not belong to this Madrassa."
            );
        }
    }

    private static boolean isBlank(
            String value
    ) {
        return value == null
                || value.trim().isEmpty();
    }
}
