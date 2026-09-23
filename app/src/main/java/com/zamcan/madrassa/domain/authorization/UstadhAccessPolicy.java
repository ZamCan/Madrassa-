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

        if (!ustadh.active) {
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

        return madrassaId.equals(
                ustadh.madrassaId
        );
    }
}
