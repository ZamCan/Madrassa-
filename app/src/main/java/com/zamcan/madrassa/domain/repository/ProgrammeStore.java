package com.zamcan.madrassa.domain.repository;

import com.zamcan.madrassa.data.model.Programme;

import java.util.List;

public interface ProgrammeStore {

    Programme findById(String programmeId);

    List<Programme> findGlobal();

    List<Programme> findActiveGlobal();

    List<Programme> findByMadrassa(
            String madrassaId
    );

    List<Programme> findActiveByMadrassa(
            String madrassaId
    );

    boolean save(Programme programme);

    boolean update(Programme programme);
}
