package com.zamcan.madrassa.domain.repository;

import com.zamcan.madrassa.data.model.Ustadh;

import java.util.List;

public interface UstadhStore {

    Ustadh findById(
            String id
    );

    List<Ustadh> findByMadrassa(
            String madrassaId
    );

    List<Ustadh> findActiveByMadrassa(
            String madrassaId
    );

    Ustadh findByPhone(
            String phone
    );

    Ustadh findActiveHeadByMadrassa(
            String madrassaId
    );

    boolean existsByPhone(
            String phone
    );

    void save(
            Ustadh ustadh
    );

    void update(
            Ustadh ustadh
    );
}
