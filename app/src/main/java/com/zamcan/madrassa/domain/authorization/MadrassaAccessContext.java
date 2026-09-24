package com.zamcan.madrassa.domain.authorization;

/** Explicit authenticated Madrassa scope for tenant-owned operations. */
public final class MadrassaAccessContext {

    private final String madrassaId;

    private MadrassaAccessContext(String madrassaId) {
        this.madrassaId = madrassaId;
    }

    public static MadrassaAccessContext forMadrassa(
            String madrassaId
    ) {
        if (madrassaId == null || madrassaId.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "madrassaId is required"
            );
        }

        return new MadrassaAccessContext(madrassaId.trim());
    }

    public String getMadrassaId() {
        return madrassaId;
    }

    public boolean allows(String value) {
        return value != null
                && !value.trim().isEmpty()
                && madrassaId.equals(value.trim());
    }
}
