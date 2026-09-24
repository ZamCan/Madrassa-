package com.zamcan.madrassa.domain.authorization;

import com.zamcan.madrassa.data.model.ApprovalStatus;
import com.zamcan.madrassa.data.model.Ustadh;

public final class UstadhAccessPolicy {

    private UstadhAccessPolicy() {
    }

    public static boolean canLogin(
            Ustadh ustadh,
            ApprovalStatus madrassaStatus
    ) {

        if (ustadh == null) {
            return false;
        }

        if (!ustadh.active
                || isBlank(ustadh.id)
                || isBlank(ustadh.madrassaId)) {
            return false;
        }

        return madrassaStatus ==
                ApprovalStatus.ACTIVE;
    }

    public static boolean belongsToMadrassa(
            Ustadh ustadh,
            String madrassaId
    ) {

        if (ustadh == null ||
                madrassaId == null) {
            return false;
        }

        if (madrassaId.trim().isEmpty()
                || ustadh.madrassaId == null
                || ustadh.madrassaId.trim().isEmpty()) {
            return false;
        }

        return madrassaId.trim().equals(
                ustadh.madrassaId.trim()
        );
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
