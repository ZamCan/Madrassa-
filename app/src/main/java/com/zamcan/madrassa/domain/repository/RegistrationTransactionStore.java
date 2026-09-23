package com.zamcan.madrassa.domain.repository;

import com.zamcan.madrassa.data.model.AccountCredential;
import com.zamcan.madrassa.data.model.Madrassa;
import com.zamcan.madrassa.data.model.PendingRegistrationCredential;
import com.zamcan.madrassa.data.model.Ustadh;

public interface RegistrationTransactionStore {

    void submit(
            Madrassa madrassa,
            PendingRegistrationCredential pendingCredential
    );

    void activate(
            Madrassa madrassa,
            Ustadh ustadh,
            AccountCredential credential,
            PendingRegistrationCredential pendingCredential
    );
}
