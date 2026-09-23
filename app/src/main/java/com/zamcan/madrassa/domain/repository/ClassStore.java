package com.zamcan.madrassa.domain.repository;

import com.zamcan.madrassa.data.model.ClassGroup;

import java.util.List;

public interface ClassStore {

    ClassGroup findById(String classId);

    List<ClassGroup> findByMadrassa(
            String madrassaId
    );

    List<ClassGroup> findActiveByMadrassa(
            String madrassaId
    );

    boolean save(ClassGroup classGroup);

    boolean update(ClassGroup classGroup);
}
