package com.zamcan.madrassa.domain.integration;

/**
 * Provider-neutral SMS boundary.
 *
 * SmsQueueService owns the local durable queue and lifecycle state.
 * A provider implementation may later consume queued messages without
 * introducing provider SDK dependencies into the domain.
 */
public interface SmsGateway {
    SmsSendResult send(SmsSendRequest request);

    final class SmsSendRequest {
        public final String messageId;
        public final String recipient;
        public final String message;
        public final String type;

        public SmsSendRequest(String messageId, String recipient,
                              String message, String type) {
            this.messageId = messageId;
            this.recipient = recipient;
            this.message = message;
            this.type = type;
        }
    }

    final class SmsSendResult {
        public final boolean accepted;
        public final String providerMessageId;
        public final String status;
        public final String message;

        private SmsSendResult(boolean accepted, String providerMessageId,
                              String status, String message) {
            this.accepted = accepted;
            this.providerMessageId = providerMessageId;
            this.status = status;
            this.message = message;
        }

        public static SmsSendResult accepted(
                String providerMessageId, String status) {
            return new SmsSendResult(true, providerMessageId, status, null);
        }

        public static SmsSendResult rejected(String status, String message) {
            return new SmsSendResult(false, null, status, message);
        }
    }
}
