package com.zamcan.madrassa.domain.authorization;

/**
 * Explicit authorization scope for academic writes.
 *
 * <p>A Madrassa context permits exactly one tenant. A Solo context
 * permits only global academic records. Keeping this scope explicit
 * prevents a repository ID supplied by a screen from becoming an
 * authorization decision by itself.</p>
 */
public final class AcademicAccessContext {

    private final String madrassaId;
    private final boolean solo;

    private AcademicAccessContext(
            String madrassaId,
            boolean solo
    ) {
        this.madrassaId = madrassaId;
        this.solo = solo;
    }

    public static AcademicAccessContext forMadrassa(
            String madrassaId
    ) {
        if (blank(madrassaId)) {
            throw new IllegalArgumentException(
                    "madrassaId is required"
            );
        }

        return new AcademicAccessContext(
                madrassaId.trim(),
                false
        );
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
