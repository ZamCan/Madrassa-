package com.zamcan.madrassa.domain.repository;

import com.zamcan.madrassa.data.model.AccountRole;
import com.zamcan.madrassa.data.model.PendingRegistrationCredential;

public interface PendingRegistrationCredentialStore {

    PendingRegistrationCredential findByMadrassaAndRole(
            String madrassaId,
            AccountRole role
    );

    boolean existsByMadrassaAndRole(
            String madrassaId,
            AccountRole role
    );

    void save(
            PendingRegistrationCredential credential
    );

    void update(
            PendingRegistrationCredential credential
    );

    void delete(
            String madrassaId,
            AccountRole role
    );
}
