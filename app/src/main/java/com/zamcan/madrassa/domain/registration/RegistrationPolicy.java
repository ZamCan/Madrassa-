package com.zamcan.madrassa.domain.registration;

import com.zamcan.madrassa.data.model.ApprovalStatus;
import com.zamcan.madrassa.data.model.Madrassa;
import com.zamcan.madrassa.domain.common.ValidationResult;

/**
 * Pure business rules for Madrassa registration.
 *
 * No Android classes.
 * No SQLite.
 * No network.
 * No UI.
 *
 * The same rules can later be reproduced/enforced
 * by the authoritative backend.
 */
public final class RegistrationPolicy {

    private RegistrationPolicy() {
    }

    public static ValidationResult validateSubmission(
            Madrassa madrassa
    ) {
        ValidationResult result =
                new ValidationResult();

        if (madrassa == null) {
            result.add("madrassa_missing");
            return result;
        }

        require(
                result,
                madrassa.name,
                "madrassa_name_required"
        );

        require(
                result,
                madrassa.type,
                "madrassa_type_required"
        );

        require(
                result,
                madrassa.administrationType,
                "administration_type_required"
        );

        require(
                result,
                madrassa.region,
                "region_required"
        );

        require(
                result,
                madrassa.district,
                "district_required"
        );

        require(
                result,
                madrassa.ward,
                "ward_required"
        );

        require(
                result,
                madrassa.area,
                "area_required"
        );

        require(
                result,
                madrassa.phone,
                "madrassa_phone_required"
        );

        require(
                result,
                madrassa.email,
                "madrassa_email_required"
        );

        if (madrassa.ustadhCount < 1) {
            result.add(
                    "ustadh_count_invalid"
            );
        }

        if (isBlank(madrassa.headUstadhId)) {
            /*
             * During the current registration flow this
             * field may temporarily contain the Head Ustadh
             * reference/name until the full Ustadh entity
             * workflow is connected.
             */
            result.add(
                    "head_ustadh_required"
            );
        }

        return result;
    }

    public static boolean canSubmit(
            ApprovalStatus status
    ) {
        return status == null ||
                status == ApprovalStatus.DRAFT ||
                status == ApprovalStatus.REJECTED;
    }

    public static boolean canStartReview(
            ApprovalStatus status
    ) {
        return status == ApprovalStatus.SUBMITTED;
    }

    public static boolean canApprove(
            ApprovalStatus status
    ) {
        return status ==
                ApprovalStatus.UNDER_REVIEW;
    }

    public static boolean canReject(
            ApprovalStatus status
    ) {
        return status ==
                ApprovalStatus.UNDER_REVIEW;
    }

    public static boolean canActivate(
            ApprovalStatus status
    ) {
        return status ==
                ApprovalStatus.APPROVED;
    }

    private static void require(
            ValidationResult result,
            String value,
            String errorCode
    ) {
        if (isBlank(value)) {
            result.add(errorCode);
        }
    }

    private static boolean isBlank(
            String value
    ) {
        return value == null ||
                value.trim().isEmpty();
    }
}
