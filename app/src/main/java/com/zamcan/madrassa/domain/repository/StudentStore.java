package com.zamcan.madrassa.domain.repository;

import com.zamcan.madrassa.data.model.Student;

import java.util.List;

public interface StudentStore {

    Student findById(String studentId);

    List<Student> findByMadrassa(
            String madrassaId
    );

    List<Student> findByParent(
            String parentId
    );

    void save(Student student);

    void update(Student student);
}
