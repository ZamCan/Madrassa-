package com.zamcan.madrassa.domain.integration;

import java.util.Collections;
import java.util.Map;

/**
 * Provider-neutral future server synchronization boundary.
 *
 * The current app remains local-first. No HTTP client, cloud SDK or
 * remote identity is introduced here. A future server implementation
 * can consume tenant-scoped operations after authentication and
 * conflict policy are explicitly added.
 */
public interface ServerSyncGateway {
    SyncResult synchronize(SyncRequest request);

    final class SyncRequest {
        public final String madrassaId;
        public final String deviceId;
        public final long cursor;
        public final Map<String, String> metadata;

        public SyncRequest(String madrassaId, String deviceId, long cursor,
                           Map<String, String> metadata) {
            this.madrassaId = madrassaId;
            this.deviceId = deviceId;
            this.cursor = cursor;
            this.metadata = metadata == null
                    ? Collections.emptyMap()
                    : Collections.unmodifiableMap(metadata);
        }
    }

    final class SyncResult {
        public final boolean accepted;
        public final long nextCursor;
        public final String status;
        public final String message;

        private SyncResult(boolean accepted, long nextCursor,
                           String status, String message) {
            this.accepted = accepted;
            this.nextCursor = nextCursor;
            this.status = status;
            this.message = message;
        }

        public static SyncResult accepted(long nextCursor, String status) {
            return new SyncResult(true, nextCursor, status, null);
        }

        public static SyncResult rejected(String status, String message) {
            return new SyncResult(false, -1L, status, message);
        }
    }
}
