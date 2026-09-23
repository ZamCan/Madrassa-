package com.zamcan.madrassa.domain.repository;

import com.zamcan.madrassa.data.model.StudentGroup;

import java.util.List;

public interface StudentGroupStore {

    StudentGroup findById(String groupId);

    List<StudentGroup> findByMadrassa(
            String madrassaId
    );

    List<StudentGroup> findActiveByMadrassa(
            String madrassaId
    );

    boolean save(StudentGroup group);

    boolean update(StudentGroup group);
}
