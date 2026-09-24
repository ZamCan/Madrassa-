package com.zamcan.madrassa.domain.communication;

import com.zamcan.madrassa.data.model.SmsMessage;
import com.zamcan.madrassa.data.model.SmsStatus;
import com.zamcan.madrassa.domain.common.IdGenerator;
import com.zamcan.madrassa.domain.common.OperationResult;
import com.zamcan.madrassa.domain.common.TenantPolicy;
import com.zamcan.madrassa.domain.repository.SmsStore;

public final class SmsQueueService {
    private final SmsStore messages;
    private final String madrassaId;

    public SmsQueueService(SmsStore messages, String madrassaId) {
        if (messages == null || blank(madrassaId)) {
            throw new IllegalArgumentException("sms store and madrassaId are required");
        }
        this.messages = messages;
        this.madrassaId = madrassaId.trim();
    }

    public OperationResult<SmsMessage> queue(SmsMessage value) {
        if (value == null || blank(value.recipient) || blank(value.message) || blank(value.type)) {
            return invalid("sms_invalid");
        }

        value.id = blank(value.id) ? IdGenerator.newId() : value.id.trim();
        value.madrassaId = madrassaId;
        value.recipient = value.recipient.trim();
        value.message = value.message.trim();
        value.type = value.type.trim();
        value.status = SmsStatus.QUEUED;
        value.createdAt = System.currentTimeMillis();
        value.sentAt = 0L;
        value.deliveredAt = 0L;

        return messages.save(value)
                ? OperationResult.success(value)
                : failed("sms_queue_failed");
    }

    public OperationResult<SmsMessage> markSending(String messageId) {
        return transition(messageId, SmsStatus.SENDING, SmsStatus.QUEUED);
    }

    public OperationResult<SmsMessage> markSent(String messageId) {
        SmsMessage value = owned(messageId);
        if (value == null) return missing("sms_missing");
        if (value.status == SmsStatus.SENT || value.status == SmsStatus.DELIVERED) {
            return OperationResult.success(value);
        }
        if (value.status != SmsStatus.SENDING) return forbidden("sms_sent_requires_sending");
        value.status = SmsStatus.SENT;
        value.sentAt = System.currentTimeMillis();
        return save(value, "sms_sent_failed");
    }

    public OperationResult<SmsMessage> markDelivered(String messageId) {
        SmsMessage value = owned(messageId);
        if (value == null) return missing("sms_missing");
        if (value.status == SmsStatus.DELIVERED) return OperationResult.success(value);
        if (value.status != SmsStatus.SENT) return forbidden("sms_delivery_requires_sent");
        value.status = SmsStatus.DELIVERED;
        value.deliveredAt = System.currentTimeMillis();
        return save(value, "sms_delivery_failed");
    }

    public OperationResult<SmsMessage> markFailed(String messageId) {
        SmsMessage value = owned(messageId);
        if (value == null) return missing("sms_missing");
        if (value.status == SmsStatus.DELIVERED) return forbidden("sms_already_delivered");
        value.status = SmsStatus.FAILED;
        return save(value, "sms_failure_update_failed");
    }

    private OperationResult<SmsMessage> transition(String id, SmsStatus next, SmsStatus expected) {
        SmsMessage value = owned(id);
        if (value == null) return missing("sms_missing");
        if (value.status == next) return OperationResult.success(value);
        if (value.status != expected) return forbidden("sms_invalid_transition");
        value.status = next;
        return save(value, "sms_transition_failed");
    }

    private SmsMessage owned(String id) {
        if (blank(id)) return null;
        SmsMessage value = messages.findById(id.trim());
        return value != null && TenantPolicy.sameMadrassa(madrassaId, value.madrassaId)
                ? value : null;
    }

    private OperationResult<SmsMessage> save(SmsMessage value, String code) {
        return messages.update(value)
                ? OperationResult.success(value)
                : failed(code);
    }

    private static <T> OperationResult<T> invalid(String code) {
        return OperationResult.validationError(code, "SMS data is invalid.");
    }
    private static <T> OperationResult<T> missing(String code) {
        return OperationResult.notFound(code, "SMS record was not found.");
    }
    private static <T> OperationResult<T> forbidden(String code) {
        return OperationResult.forbidden(code, "SMS operation is not authorized.");
    }
    private static <T> OperationResult<T> failed(String code) {
        return OperationResult.failed(code, "SMS operation failed.");
    }
    private static boolean blank(String v) { return v == null || v.trim().isEmpty(); }
}
