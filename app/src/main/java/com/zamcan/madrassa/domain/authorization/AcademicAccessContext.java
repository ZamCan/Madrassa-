package com.zamcan.madrassa.domain.authorization;

/**
 * Explicit tenant scope for academic write operations.
 *
 * A Madrassa context authorizes one exact tenant. A Solo context authorizes
 * only global academic records (madrassaId == null). An unbound context is
 * intentionally not a usable write scope.
 */
public final class AcademicAccessContext {

    private final String madrassaId;
    private final boolean solo;

    private AcademicAccessContext(String madrassaId, boolean solo) {
        this.madrassaId = madrassaId;
        this.solo = solo;
    }

    public static AcademicAccessContext forMadrassa(String madrassaId) {
        if (blank(madrassaId)) {
            throw new IllegalArgumentException("madrassaId is required.");
        }
        return new AcademicAccessContext(madrassaId.trim(), false);
    }

    public static AcademicAccessContext forSolo() {
        return new AcademicAccessContext(null, true);
    }

    public String getMadrassaId() {
        return madrassaId;
    }

    public boolean isSolo() {
        return solo;
    }

    public boolean allowsMadrassa(String value) {
        return !solo
                && !blank(value)
                && madrassaId.equals(value.trim());
    }

    public boolean allowsGlobal() {
        return solo;
    }

    private static boolean blank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
