package com.zamcan.madrassa.domain.repository;

import com.zamcan.madrassa.data.model.Student;

/** Atomic persistence boundary for admitting a student and its required links. */
public interface StudentAdmissionStore {

    void admit(
            Student student,
            String parentId
    );
}
