package com.zamcan.madrassa.domain.registration;

import com.zamcan.madrassa.data.model.AccountCredential;
import com.zamcan.madrassa.data.model.AccountRole;
import com.zamcan.madrassa.data.model.ApprovalStatus;
import com.zamcan.madrassa.data.model.Madrassa;
import com.zamcan.madrassa.data.model.PendingRegistrationCredential;
import com.zamcan.madrassa.data.model.Ustadh;
import com.zamcan.madrassa.domain.auth.CredentialPolicy;
import com.zamcan.madrassa.domain.common.IdGenerator;
import com.zamcan.madrassa.domain.common.OperationResult;
import com.zamcan.madrassa.domain.common.ValidationResult;
import com.zamcan.madrassa.domain.repository.MadrassaStore;
import com.zamcan.madrassa.domain.repository.PendingRegistrationCredentialStore;
import com.zamcan.madrassa.domain.repository.RegistrationTransactionStore;
import com.zamcan.madrassa.domain.auth.PasswordVerifier;

public final class RegistrationService {

    private final MadrassaStore madrassaStore;
    private final PendingRegistrationCredentialStore pendingStore;
    private final RegistrationTransactionStore transactionStore;
    private final PasswordVerifier passwordVerifier;

    public RegistrationService(
            MadrassaStore madrassaStore,
            PendingRegistrationCredentialStore pendingStore,
            RegistrationTransactionStore transactionStore,
            PasswordVerifier passwordVerifier
    ) {
        if (madrassaStore == null ||
                pendingStore == null ||
                transactionStore == null ||
                passwordVerifier == null) {
            throw new IllegalArgumentException(
                    "Registration dependencies must not be null."
            );
        }

        this.madrassaStore = madrassaStore;
        this.pendingStore = pendingStore;
        this.transactionStore = transactionStore;
        this.passwordVerifier = passwordVerifier;
    }

    public OperationResult<Madrassa> submit(
            RegistrationSubmission submission
    ) {
        if (submission == null ||
                submission.madrassa == null) {
            return OperationResult.validationError(
                    "registration_missing",
                    "Registration data is required."
            );
        }

        Madrassa madrassa =
                submission.madrassa;

        ValidationResult validation =
                RegistrationPolicy.validateSubmission(
                        madrassa
                );

        if (!validation.isValid()) {
            return OperationResult.validationError(
                    validation.getErrors().get(0),
                    "Registration data is incomplete."
            );
        }

        if (!CredentialPolicy.validNewPassword(
                AccountRole.USTADH,
                submission.initialPassword
        )) {
            return OperationResult.validationError(
                    "invalid_initial_password",
                    "Initial password does not meet the required security rules."
            );
        }

        if (!RegistrationPolicy.canSubmit(
                madrassa.approvalStatus
        )) {
            return OperationResult.conflict(
                    "invalid_registration_state",
                    "Madrassa cannot be submitted from its current status."
            );
        }

        if (madrassaStore.existsByName(
                madrassa.name
        )) {
            return OperationResult.conflict(
                    "madrassa_name_exists",
                    "A Madrassa with this name already exists."
            );
        }

        if (madrassa.id == null ||
                madrassa.id.trim().isEmpty()) {
            madrassa.id =
                    IdGenerator.newId();
        }

        long now =
                System.currentTimeMillis();

        PasswordVerifier.PasswordHashResult hash =
                passwordVerifier.hash(
                        submission.initialPassword
                );

        PendingRegistrationCredential pending =
                new PendingRegistrationCredential();

        pending.id =
                IdGenerator.newId();

        pending.madrassaId =
                madrassa.id;

        pending.role =
                AccountRole.USTADH;

        pending.passwordHash =
                hash.hash;

        pending.passwordSalt =
                hash.salt;

        pending.createdAt =
                now;

        pending.updatedAt =
                now;

        madrassa.approvalStatus =
                ApprovalStatus.SUBMITTED;

        madrassa.rejectionReason =
                null;

        madrassa.submittedAt =
                now;

        madrassa.approvedAt =
                0L;

        try {
            transactionStore.submit(
                    madrassa,
                    pending
            );
        } catch (RuntimeException error) {
            return OperationResult.failed(
                    "registration_save_failed",
                    "Madrassa registration could not be saved."
            );
        }

        return OperationResult.success(
                madrassa
        );
    }

    public OperationResult<Madrassa> startReview(
            String madrassaId
    ) {
        Madrassa madrassa =
                madrassaStore.findById(
                        madrassaId
                );

        if (madrassa == null) {
            return OperationResult.notFound(
                    "madrassa_not_found",
                    "Madrassa was not found."
            );
        }

        if (!RegistrationPolicy.canStartReview(
                madrassa.approvalStatus
        )) {
            return OperationResult.conflict(
                    "invalid_review_state",
                    "Madrassa is not awaiting review."
            );
        }

        boolean updated =
                madrassaStore.updateApprovalStatus(
                        madrassa.id,
                        ApprovalStatus.UNDER_REVIEW,
                        null
                );

        if (!updated) {
            return OperationResult.failed(
                    "review_update_failed",
                    "Unable to start Madrassa review."
            );
        }

        madrassa.approvalStatus =
                ApprovalStatus.UNDER_REVIEW;

        return OperationResult.success(
                madrassa
        );
    }

    public OperationResult<Madrassa> approve(
            String madrassaId
    ) {
        Madrassa madrassa =
                madrassaStore.findById(
                        madrassaId
                );

        if (madrassa == null) {
            return OperationResult.notFound(
                    "madrassa_not_found",
                    "Madrassa was not found."
            );
        }

        if (!RegistrationPolicy.canApprove(
                madrassa.approvalStatus
        )) {
            return OperationResult.conflict(
                    "invalid_approval_state",
                    "Madrassa cannot be approved from its current status."
            );
        }

        boolean updated =
                madrassaStore.updateApprovalStatus(
                        madrassa.id,
                        ApprovalStatus.APPROVED,
                        null
                );

        if (!updated) {
            return OperationResult.failed(
                    "approval_update_failed",
                    "Unable to approve Madrassa."
            );
        }

        madrassa.approvalStatus =
                ApprovalStatus.APPROVED;

        madrassa.rejectionReason =
                null;

        madrassa.approvedAt =
                System.currentTimeMillis();

        return OperationResult.success(
                madrassa
        );
    }

    public OperationResult<Madrassa> reject(
            String madrassaId,
            String reason
    ) {
        if (reason == null ||
                reason.trim().isEmpty()) {
            return OperationResult.validationError(
                    "rejection_reason_required",
                    "A rejection reason is required."
            );
        }

        Madrassa madrassa =
                madrassaStore.findById(
                        madrassaId
                );

        if (madrassa == null) {
            return OperationResult.notFound(
                    "madrassa_not_found",
                    "Madrassa was not found."
            );
        }

        if (!RegistrationPolicy.canReject(
                madrassa.approvalStatus
        )) {
            return OperationResult.conflict(
                    "invalid_rejection_state",
                    "Madrassa cannot be rejected from its current status."
            );
        }

        String cleanReason =
                reason.trim();

        boolean updated =
                madrassaStore.updateApprovalStatus(
                        madrassa.id,
                        ApprovalStatus.REJECTED,
                        cleanReason
                );

        if (!updated) {
            return OperationResult.failed(
                    "rejection_update_failed",
                    "Unable to reject Madrassa."
            );
        }

        madrassa.approvalStatus =
                ApprovalStatus.REJECTED;

        madrassa.rejectionReason =
                cleanReason;

        return OperationResult.success(
                madrassa
        );
    }

    public OperationResult<Madrassa> activate(
            String madrassaId
    ) {
        Madrassa madrassa =
                madrassaStore.findById(
                        madrassaId
                );

        if (madrassa == null) {
            return OperationResult.notFound(
                    "madrassa_not_found",
                    "Madrassa was not found."
            );
        }

        if (!RegistrationPolicy.canActivate(
                madrassa.approvalStatus
        )) {
            return OperationResult.conflict(
                    "invalid_activation_state",
                    "Madrassa must be approved before activation."
            );
        }

        PendingRegistrationCredential pending =
                pendingStore.findByMadrassaAndRole(
                        madrassa.id,
                        AccountRole.USTADH
                );

        if (pending == null) {
            return OperationResult.failed(
                    "pending_credential_missing",
                    "Madrassa cannot be activated because its registration credential is missing."
            );
        }

        String ustadhId =
                IdGenerator.newId();

        Ustadh ustadh =
                new Ustadh();

        ustadh.id =
                ustadhId;

        ustadh.madrassaId =
                madrassa.id;

        ustadh.fullName =
                madrassa.headUstadhId;

        ustadh.phone =
                madrassa.phone;

        ustadh.email =
                madrassa.email;

        ustadh.headUstadh =
                true;

        ustadh.active =
                true;

        long now =
                System.currentTimeMillis();

        AccountCredential credential =
                new AccountCredential();

        credential.id =
                IdGenerator.newId();

        credential.accountId =
                ustadh.id;

        credential.role =
                AccountRole.USTADH;

        credential.madrassaId =
                madrassa.id;

        credential.passwordHash =
                pending.passwordHash;

        credential.passwordSalt =
                pending.passwordSalt;

        credential.active =
                true;

        credential.firstLogin =
                true;

        credential.createdAt =
                now;

        credential.updatedAt =
                now;

        credential.lastLoginAt =
                0L;

        try {
            transactionStore.activate(
                    madrassa,
                    ustadh,
                    credential,
                    pending
            );
        } catch (RuntimeException error) {
            return OperationResult.failed(
                    "activation_failed",
                    "Madrassa activation could not be completed."
            );
        }

        madrassa.headUstadhId =
                ustadh.id;

        madrassa.approvalStatus =
                ApprovalStatus.ACTIVE;

        return OperationResult.success(
                madrassa
        );
    }
}
