package com.zamcan.madrassa.domain.repository;

import com.zamcan.madrassa.data.model.StudentGroupMember;

import java.util.List;

public interface StudentGroupMemberStore {

    List<StudentGroupMember> findByGroup(
            String groupId
    );

    List<StudentGroupMember> findByStudent(
            String studentId
    );

    boolean exists(
            String groupId,
            String studentId
    );

    boolean add(
            StudentGroupMember member
    );

    boolean remove(
            String groupId,
            String studentId
    );
}
