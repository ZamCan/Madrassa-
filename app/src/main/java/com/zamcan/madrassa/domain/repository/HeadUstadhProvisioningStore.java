package com.zamcan.madrassa.domain.repository;

import com.zamcan.madrassa.data.model.AccountCredential;
import com.zamcan.madrassa.data.model.Madrassa;
import com.zamcan.madrassa.data.model.Ustadh;

public interface HeadUstadhProvisioningStore {

    void provision(
            Madrassa madrassa,
            Ustadh ustadh,
            AccountCredential credential
    );
}
