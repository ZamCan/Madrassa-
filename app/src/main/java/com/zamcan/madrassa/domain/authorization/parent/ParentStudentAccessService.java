package com.zamcan.madrassa.domain.authorization.parent;

import com.zamcan.madrassa.data.model.Parent;
import com.zamcan.madrassa.data.model.Student;
import com.zamcan.madrassa.domain.repository.ParentStore;
import com.zamcan.madrassa.domain.repository.StudentStore;

public final class ParentStudentAccessService {

    private final ParentStore parentStore;
    private final StudentStore studentStore;

    public ParentStudentAccessService(
            ParentStore parentStore,
            StudentStore studentStore
    ) {
        this.parentStore = parentStore;
        this.studentStore = studentStore;
    }

    public boolean canAccessStudent(
            String parentId,
            String studentId
    ) {
        if (parentId == null
                || studentId == null) {
            return false;
        }

        Parent parent =
                parentStore.findById(
                        parentId.trim()
                );

        Student student =
                studentStore.findById(
                        studentId.trim()
                );

        return ParentAccessPolicy.canAccessStudent(
                parent,
                student
        );
    }

    public boolean canAccessLinkedStudent(
            String parentId,
            String studentId
    ) {
        if (!canAccessStudent(
                parentId,
                studentId
        )) {
            return false;
        }

        Parent parent =
                parentStore.findById(
                        parentId.trim()
                );

        return ParentAccessPolicy.ownsStudent(
                parent,
                studentId
        );
    }

    public Student requireStudentAccess(
            String parentId,
            String studentId
    ) {
        if (!canAccessStudent(
                parentId,
                studentId
        )) {
            throw new SecurityException(
                    "Parent is not authorized to access this student."
            );
        }

        Student student =
                studentStore.findById(
                        studentId.trim()
                );

        if (student == null) {
            throw new SecurityException(
                    "Student is no longer available."
            );
        }

        return student;
    }

    public Student requireLinkedStudentAccess(
            String parentId,
            String studentId
    ) {
        if (!canAccessLinkedStudent(
                parentId,
                studentId
        )) {
            throw new SecurityException(
                    "Student is not linked to this parent."
            );
        }

        return studentStore.findById(
                studentId.trim()
        );
    }
}
