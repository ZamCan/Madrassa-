package com.zamcan.madrassa.domain.programme;

import com.zamcan.madrassa.data.model.Programme;
import com.zamcan.madrassa.domain.common.IdGenerator;
import com.zamcan.madrassa.domain.repository.ProgrammeStore;

import java.util.ArrayList;
import java.util.List;

/**
 * Idempotently creates the three standard tenant-owned programmes.
 *
 * <p>Existing records are reused; this service never replaces a caller's
 * programme list and never creates a global Solo record for a Madrassa.</p>
 */
public final class ProgrammeSetupService {

    private static final String QURAN = "Qur'an";
    private static final String TAWHEED = "Tawheed";
    private static final String FIQH = "Fiqh";

    private final ProgrammeStore programmeStore;

    public ProgrammeSetupService(ProgrammeStore programmeStore) {
        if (programmeStore == null) {
            throw new IllegalArgumentException(
                    "programmeStore is required"
            );
        }
        this.programmeStore = programmeStore;
    }

    public List<Programme> ensureMadrassaDefaults(
            String madrassaId
    ) {
        if (blank(madrassaId)) {
            throw new IllegalArgumentException(
                    "madrassaId is required"
            );
        }

        String tenantId = madrassaId.trim();
        List<Programme> existing =
                new ArrayList<>(programmeStore.findByMadrassa(tenantId));
        List<Programme> result = new ArrayList<>();

        result.add(ensure(existing, tenantId, QURAN));
        result.add(ensure(existing, tenantId, TAWHEED));
        result.add(ensure(existing, tenantId, FIQH));

        return result;
    }

    private Programme ensure(
            List<Programme> existing,
            String madrassaId,
            String name
    ) {
        for (Programme programme : existing) {
            if (programme != null
                    && !blank(programme.name)
                    && name.equalsIgnoreCase(programme.name.trim())) {
                return programme;
            }
        }

        Programme programme = new Programme();
        programme.id = IdGenerator.newId();
        programme.madrassaId = madrassaId;
        programme.name = name;
        programme.category = "ISLAMIC";
        programme.defaultProgramme = true;
        programme.active = true;

        if (!programmeStore.save(programme)) {
            throw new IllegalStateException(
                    "Unable to create default programme: " + name
            );
        }

        existing.add(programme);
        return programme;
    }

    private static boolean blank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
