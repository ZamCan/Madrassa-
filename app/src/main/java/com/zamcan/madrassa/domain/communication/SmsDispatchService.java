package com.zamcan.madrassa.domain.communication;

import com.zamcan.madrassa.data.model.SmsMessage;
import com.zamcan.madrassa.domain.common.OperationResult;
import com.zamcan.madrassa.domain.integration.SmsGateway;
import com.zamcan.madrassa.domain.repository.SmsStore;

/**
 * Bridges the durable local SMS queue to a provider-neutral gateway.
 *
 * The queue remains the source of local truth. A failed provider attempt
 * leaves the message retryable rather than deleting or silently losing it.
 */
public final class SmsDispatchService {
    private final SmsStore messages;
    private final SmsQueueService queue;
    private final SmsGateway gateway;
    private final String madrassaId;

    public SmsDispatchService(
            SmsStore messages,
            SmsQueueService queue,
            SmsGateway gateway,
            String madrassaId
    ) {
        if (messages == null || queue == null || gateway == null
                || blank(madrassaId)) {
            throw new IllegalArgumentException("SMS dispatch dependencies are required");
        }
        this.messages = messages;
        this.queue = queue;
        this.gateway = gateway;
        this.madrassaId = madrassaId.trim();
    }

    public OperationResult<SmsMessage> dispatch(String messageId) {
        if (blank(messageId)) {
            return OperationResult.validationError(
                    "sms_message_id_required",
                    "SMS message ID is required."
            );
        }

        SmsMessage value = messages.findById(messageId.trim());
        if (value == null) {
            return OperationResult.notFound(
                    "sms_missing",
                    "SMS message was not found."
            );
        }

        if (!madrassaId.equals(value.madrassaId)) {
            return OperationResult.forbidden(
                    "sms_tenant_mismatch",
                    "SMS message is outside the authorized Madrassa."
            );
        }

        if (value.status == com.zamcan.madrassa.data.model.SmsStatus.DELIVERED) {
            return OperationResult.success(value);
        }

        if (value.status == com.zamcan.madrassa.data.model.SmsStatus.SENT) {
            return OperationResult.success(value);
        }

        OperationResult<SmsMessage> sending = queue.markSending(value.id);
        if (!sending.isSuccess()) {
            return sending;
        }

        value = sending.getData();

        SmsGateway.SmsSendResult result;
        try {
            result = gateway.send(
                    new SmsGateway.SmsSendRequest(
                            value.id,
                            value.recipient,
                            value.message,
                            value.type
                    )
            );
        } catch (RuntimeException e) {
            queue.markFailed(value.id);
            return OperationResult.failed(
                    "sms_provider_exception",
                    "SMS provider failed unexpectedly."
            );
        }

        if (result == null || !result.accepted) {
            queue.markFailed(value.id);
            return OperationResult.failed(
                    "sms_provider_rejected",
                    result == null
                            ? "SMS provider returned no result."
                            : result.message
            );
        }

        value.provider = result.status;
        return queue.markSent(value.id);
    }

    private static boolean blank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
