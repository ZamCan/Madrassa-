package com.zamcan.madrassa.domain.repository;

import com.zamcan.madrassa.data.model.AccountCredential;
import com.zamcan.madrassa.data.model.AccountRole;

public interface CredentialStore {

    AccountCredential findById(
            String id
    );

    AccountCredential findByAccount(
            String accountId,
            AccountRole role
    );

    boolean existsByAccount(
            String accountId,
            AccountRole role
    );

    void save(
            AccountCredential credential
    );

    void update(
            AccountCredential credential
    );
}
