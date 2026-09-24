package com.zamcan.madrassa.domain.students;

import com.zamcan.madrassa.data.model.Student;
import com.zamcan.madrassa.data.model.StudentGroup;
import com.zamcan.madrassa.data.model.StudentGroupMember;
import com.zamcan.madrassa.domain.common.TenantPolicy;
import com.zamcan.madrassa.domain.repository.StudentGroupMemberStore;
import com.zamcan.madrassa.domain.repository.StudentGroupStore;
import com.zamcan.madrassa.domain.repository.StudentStore;

import java.util.List;

public final class StudentGroupService {

    private final StudentGroupStore groupStore;
    private final StudentGroupMemberStore memberStore;
    private final StudentStore studentStore;

    public StudentGroupService(
            StudentGroupStore groupStore,
            StudentGroupMemberStore memberStore,
            StudentStore studentStore
    ) {
        if (groupStore == null
                || memberStore == null
                || studentStore == null) {
            throw new IllegalArgumentException(
                    "group stores and studentStore are required"
            );
        }

        this.groupStore = groupStore;
        this.memberStore = memberStore;
        this.studentStore = studentStore;
    }

    public boolean create(
            StudentGroup group
    ) {
        validateGroup(group);

        if (group.createdAt <= 0) {
            group.createdAt =
                    System.currentTimeMillis();
        }

        if (group.updatedAt <= 0) {
            group.updatedAt =
                    group.createdAt;
        }

        return groupStore.save(group);
    }

    public boolean update(
            StudentGroup group
    ) {
        validateGroup(group);

        StudentGroup existing =
                groupStore.findById(group.id);

        if (existing == null) {
            throw new IllegalArgumentException(
                    "student group does not exist"
            );
        }

        TenantPolicy.requireSameMadrassa(
                group.madrassaId,
                existing.madrassaId
        );

        group.updatedAt =
                System.currentTimeMillis();

        return groupStore.update(group);
    }

    public boolean setActive(
            String madrassaId,
            String groupId,
            boolean active
    ) {
        StudentGroup group =
                findById(madrassaId, groupId);

        if (group == null) {
            throw new IllegalArgumentException(
                    "student group does not exist"
            );
        }

        group.active = active;
        group.updatedAt =
                System.currentTimeMillis();

        return groupStore.update(group);
    }

    public StudentGroup findById(
            String madrassaId,
            String groupId
    ) {
        if (blank(madrassaId)
                || blank(groupId)) {
            return null;
        }

        StudentGroup group =
                groupStore.findById(
                        groupId.trim()
                );

        if (group == null) {
            return null;
        }

        TenantPolicy.requireSameMadrassa(
                madrassaId.trim(),
                group.madrassaId
        );

        return group;
    }

    public boolean addStudent(
            String madrassaId,
            String groupId,
            String studentId
    ) {
        StudentGroup group =
                findById(madrassaId, groupId);

        if (group == null) {
            throw new IllegalArgumentException(
                    "student group does not exist"
            );
        }

        if (!group.active) {
            throw new IllegalArgumentException(
                    "student group is inactive"
            );
        }

        Student student =
                studentStore.findById(
                        studentId
                );

        if (student == null) {
            throw new IllegalArgumentException(
                    "student does not exist"
            );
        }

        TenantPolicy.requireSameMadrassa(
                madrassaId.trim(),
                student.madrassaId
        );

        if (memberStore.exists(
                group.id,
                student.id
        )) {
            return false;
        }

        StudentGroupMember member =
                new StudentGroupMember();

        member.groupId =
                group.id;

        member.studentId =
                student.id;

        member.createdAt =
                System.currentTimeMillis();

        boolean added =
                memberStore.add(member);

        if (added) {
            group.updatedAt =
                    System.currentTimeMillis();

            groupStore.update(group);
        }

        return added;
    }

    public boolean removeStudent(
            String madrassaId,
            String groupId,
            String studentId
    ) {
        StudentGroup group =
                findById(madrassaId, groupId);

        if (group == null) {
            throw new IllegalArgumentException(
                    "student group does not exist"
            );
        }

        Student student =
                requireStudent(madrassaId, studentId);

        boolean removed =
                memberStore.remove(
                        group.id,
                        student.id
                );

        if (removed) {
            group.updatedAt =
                    System.currentTimeMillis();

            groupStore.update(group);
        }

        return removed;
    }

    public List<StudentGroup> findActive(
            String madrassaId
    ) {
        if (blank(madrassaId)) {
            return java.util.Collections.emptyList();
        }

        return groupStore.findActiveByMadrassa(
                madrassaId.trim()
        );
    }

    public List<StudentGroupMember> members(
            String madrassaId,
            String groupId
    ) {
        StudentGroup group =
                findById(madrassaId, groupId);

        if (group == null) {
            return java.util.Collections.emptyList();
        }

        return memberStore.findByGroup(
                group.id
        );
    }

    private Student requireStudent(
            String madrassaId,
            String studentId
    ) {
        if (blank(studentId)) {
            throw new IllegalArgumentException(
                    "studentId is required"
            );
        }

        Student student =
                studentStore.findById(studentId.trim());

        if (student == null) {
            throw new IllegalArgumentException(
                    "student does not exist"
            );
        }

        if (!student.active) {
            throw new SecurityException(
                    "student is inactive"
            );
        }

        TenantPolicy.requireSameMadrassa(
                madrassaId,
                student.madrassaId
        );

        return student;
    }

    private static void validateGroup(
            StudentGroup group
    ) {
        if (group == null) {
            throw new IllegalArgumentException(
                    "student group is required"
            );
        }

        if (blank(group.id)) {
            throw new IllegalArgumentException(
                    "student group id is required"
            );
        }

        if (blank(group.madrassaId)) {
            throw new IllegalArgumentException(
                    "madrassa id is required"
            );
        }

        if (blank(group.name)) {
            throw new IllegalArgumentException(
                    "student group name is required"
            );
        }

        group.id =
                group.id.trim();

        group.madrassaId =
                group.madrassaId.trim();

        group.name =
                group.name.trim();
    }

    private static boolean blank(
            String value
    ) {
        return value == null
                || value.trim().isEmpty();
    }
}
