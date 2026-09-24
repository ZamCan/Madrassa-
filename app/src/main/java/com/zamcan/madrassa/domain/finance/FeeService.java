package com.zamcan.madrassa.domain.finance;

import com.zamcan.madrassa.data.model.Fee;
import com.zamcan.madrassa.data.model.FeeStatus;
import com.zamcan.madrassa.data.model.Student;
import com.zamcan.madrassa.domain.common.IdGenerator;
import com.zamcan.madrassa.domain.common.OperationResult;
import com.zamcan.madrassa.domain.common.TenantPolicy;
import com.zamcan.madrassa.domain.repository.FeeStore;
import com.zamcan.madrassa.domain.repository.StudentStore;

public final class FeeService {
    private final FeeStore fees;
    private final StudentStore students;
    private final String madrassaId;

    public FeeService(FeeStore fees, StudentStore students, String madrassaId) {
        if (fees == null || students == null || blank(madrassaId)) {
            throw new IllegalArgumentException("fee dependencies and madrassaId are required");
        }
        this.fees = fees;
        this.students = students;
        this.madrassaId = madrassaId.trim();
    }

    public OperationResult<Fee> createFee(Fee fee) {
        if (fee == null || blank(fee.studentId) || blank(fee.type) || fee.amount < 0) {
            return invalid("fee_invalid");
        }

        Student student = students.findById(fee.studentId.trim());
        if (student == null) return missing("student_missing");
        if (!student.active) return forbidden("student_inactive");
        if (!TenantPolicy.sameMadrassa(madrassaId, student.madrassaId)) {
            return forbidden("fee_student_tenant_mismatch");
        }

        fee.id = blank(fee.id) ? IdGenerator.newId() : fee.id.trim();
        fee.studentId = student.id;
        fee.madrassaId = madrassaId;
        fee.type = fee.type.trim();
        fee.status = FeeStatus.UNPAID;
        fee.submittedAt = 0L;
        fee.confirmedAt = 0L;
        fee.lockedAt = 0L;

        return fees.save(fee)
                ? OperationResult.success(fee)
                : failed("fee_save_failed");
    }

    public OperationResult<Fee> submitPayment(String feeId, String submittedBy) {
        Fee fee = ownedFee(feeId);
        if (fee == null) return missing("fee_missing");

        if (fee.status == FeeStatus.PENDING
                || fee.status == FeeStatus.CONFIRMED
                || fee.status == FeeStatus.PAID) {
            return OperationResult.success(fee);
        }

        if (fee.status == FeeStatus.CANCELLED || fee.status == FeeStatus.LOCKED) {
            return forbidden("fee_not_payable");
        }

        fee.status = FeeStatus.PENDING;
        fee.submittedAt = System.currentTimeMillis();
        fee.submittedBy = blank(submittedBy) ? null : submittedBy.trim();

        return fees.update(fee)
                ? OperationResult.success(fee)
                : failed("fee_submission_failed");
    }

    public OperationResult<Fee> confirmPayment(String feeId, String confirmedBy) {
        Fee fee = ownedFee(feeId);
        if (fee == null) return missing("fee_missing");

        if (fee.status == FeeStatus.CONFIRMED || fee.status == FeeStatus.PAID) {
            return OperationResult.success(fee);
        }

        if (fee.status != FeeStatus.PENDING) {
            return forbidden("fee_confirmation_requires_pending");
        }

        fee.status = FeeStatus.CONFIRMED;
        fee.confirmedAt = System.currentTimeMillis();
        fee.confirmedBy = blank(confirmedBy) ? null : confirmedBy.trim();

        return fees.update(fee)
                ? OperationResult.success(fee)
                : failed("fee_confirmation_failed");
    }

    public OperationResult<Fee> cancel(String feeId) {
        Fee fee = ownedFee(feeId);
        if (fee == null) return missing("fee_missing");

        if (fee.status == FeeStatus.PAID || fee.status == FeeStatus.LOCKED) {
            return forbidden("fee_cannot_be_cancelled");
        }

        if (fee.status == FeeStatus.CANCELLED) return OperationResult.success(fee);

        fee.status = FeeStatus.CANCELLED;
        return fees.update(fee)
                ? OperationResult.success(fee)
                : failed("fee_cancel_failed");
    }

    public OperationResult<Fee> lock(String feeId) {
        Fee fee = ownedFee(feeId);
        if (fee == null) return missing("fee_missing");

        if (fee.status != FeeStatus.CONFIRMED && fee.status != FeeStatus.PAID) {
            return forbidden("fee_lock_requires_confirmed");
        }

        fee.status = FeeStatus.LOCKED;
        fee.lockedAt = System.currentTimeMillis();

        return fees.update(fee)
                ? OperationResult.success(fee)
                : failed("fee_lock_failed");
    }

    private Fee ownedFee(String feeId) {
        if (blank(feeId)) return null;
        Fee fee = fees.findById(feeId.trim());
        if (fee == null) return null;
        if (!TenantPolicy.sameMadrassa(madrassaId, fee.madrassaId)) {
            return null;
        }
        return fee;
    }

    private static <T> OperationResult<T> invalid(String code) {
        return OperationResult.validationError(code, "Finance data is invalid.");
    }
    private static <T> OperationResult<T> missing(String code) {
        return OperationResult.notFound(code, "Finance record was not found.");
    }
    private static <T> OperationResult<T> forbidden(String code) {
        return OperationResult.forbidden(code, "Finance operation is not authorized.");
    }
    private static <T> OperationResult<T> failed(String code) {
        return OperationResult.failure(code, "Finance operation failed.");
    }
    private static boolean blank(String v) { return v == null || v.trim().isEmpty(); }
}
