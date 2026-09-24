package com.zamcan.madrassa.domain.authorization;

/**
 * Explicit authenticated Madrassa scope for domain writes.
 *
 * A caller must bind a concrete Madrassa before performing tenant-owned
 * operations. IDs supplied to a service are relationship references, not
 * authorization credentials.
 */
public final class MadrassaAccessContext {
    private final String madrassaId;

    private MadrassaAccessContext(String madrassaId) {
        this.madrassaId = madrassaId;
    }

    public static MadrassaAccessContext forMadrassa(String madrassaId) {
        if (madrassaId == null || madrassaId.trim().isEmpty()) {
            throw new IllegalArgumentException("madrassaId is required.");
        }
        return new MadrassaAccessContext(madrassaId.trim());
    }

    public String getMadrassaId() {
        return madrassaId;
    }

    public boolean allows(String value) {
        return value != null && madrassaId.equals(value.trim());
    }
}
