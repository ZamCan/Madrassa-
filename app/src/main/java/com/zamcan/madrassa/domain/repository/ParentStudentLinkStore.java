package com.zamcan.madrassa.domain.repository;

public interface ParentStudentLinkStore {

    void link(
            String parentId,
            String studentId
    );

    void unlink(
            String parentId,
            String studentId
    );
}
