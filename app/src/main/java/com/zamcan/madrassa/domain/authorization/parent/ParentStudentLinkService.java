package com.zamcan.madrassa.domain.authorization.parent;

import com.zamcan.madrassa.data.model.Parent;
import com.zamcan.madrassa.data.model.Student;
import com.zamcan.madrassa.domain.common.TenantPolicy;
import com.zamcan.madrassa.domain.repository.ParentStore;
import com.zamcan.madrassa.domain.repository.ParentStudentLinkStore;
import com.zamcan.madrassa.domain.repository.StudentStore;

public final class ParentStudentLinkService {

    private final ParentStore parentStore;
    private final StudentStore studentStore;
    private final ParentStudentLinkStore linkStore;

    public ParentStudentLinkService(
            ParentStore parentStore,
            StudentStore studentStore,
            ParentStudentLinkStore linkStore
    ) {
        this.parentStore = parentStore;
        this.studentStore = studentStore;
        this.linkStore = linkStore;
    }

    public void link(
            String parentId,
            String studentId
    ) {
        Parent parent =
                requireParent(parentId);

        Student student =
                requireStudent(studentId);

        TenantPolicy.requireSameMadrassa(
                parent.madrassaId,
                student.madrassaId
        );

        linkStore.link(
                parent.id.trim(),
                student.id.trim()
        );
    }

    public void unlink(
            String parentId,
            String studentId
    ) {
        Parent parent =
                requireParent(parentId);

        Student student =
                requireStudent(studentId);

        TenantPolicy.requireSameMadrassa(
                parent.madrassaId,
                student.madrassaId
        );

        linkStore.unlink(
                parent.id.trim(),
                student.id.trim()
        );
    }

    private Parent requireParent(
            String parentId
    ) {
        if (isBlank(parentId)) {
            throw new IllegalArgumentException(
                    "parentId is required."
            );
        }

        Parent parent =
                parentStore.findById(
                        parentId.trim()
                );

        if (parent == null) {
            throw new IllegalArgumentException(
                    "Parent not found."
            );
        }

        if (!parent.active) {
            throw new SecurityException(
                    "Parent is inactive."
            );
        }

        return parent;
    }

    private Student requireStudent(
            String studentId
    ) {
        if (isBlank(studentId)) {
            throw new IllegalArgumentException(
                    "studentId is required."
            );
        }

        Student student =
                studentStore.findById(
                        studentId.trim()
                );

        if (student == null) {
            throw new IllegalArgumentException(
                    "Student not found."
            );
        }

        if (!student.active) {
            throw new SecurityException(
                    "Student is inactive."
            );
        }

        return student;
    }

    private static boolean isBlank(
            String value
    ) {
        return value == null
                || value.trim().isEmpty();
    }
}
