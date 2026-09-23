package com.zamcan.madrassa.domain.common;

import java.util.UUID;

/**
 * Generates globally unique application identifiers.
 *
 * IDs are generated independently from SQLite so that the
 * same domain model can later synchronize with a remote backend.
 */
public final class IdGenerator {

    private IdGenerator() {
    }

    public static String newId() {
        return UUID.randomUUID()
                .toString()
                .replace("-", "");
    }
}
