package com.zamcan.madrassa.domain.repository;

import com.zamcan.madrassa.data.model.AccountCredential;
import com.zamcan.madrassa.data.model.Parent;

public interface ParentProvisioningStore {

    void provision(
            Parent parent,
            AccountCredential credential
    );
}
