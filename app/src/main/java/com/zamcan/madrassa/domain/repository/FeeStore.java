package com.zamcan.madrassa.domain.repository;

import com.zamcan.madrassa.data.model.Fee;

import java.util.List;

public interface FeeStore {

    Fee findById(String feeId);

    List<Fee> findByStudent(
            String studentId
    );

    List<Fee> findByMadrassa(
            String madrassaId
    );

    boolean save(Fee fee);

    boolean update(Fee fee);
}
