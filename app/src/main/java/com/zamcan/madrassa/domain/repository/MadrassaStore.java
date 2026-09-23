package com.zamcan.madrassa.domain.repository;

import com.zamcan.madrassa.data.model.ApprovalStatus;
import com.zamcan.madrassa.data.model.Madrassa;

import java.util.List;

public interface MadrassaStore {

    boolean existsById(String id);

    boolean existsByName(String name);

    Madrassa findById(String id);

    Madrassa findByName(String name);

    List<Madrassa> findAll();

    boolean save(Madrassa madrassa);

    boolean updateApprovalStatus(
            String madrassaId,
            ApprovalStatus status,
            String rejectionReason
    );
}
