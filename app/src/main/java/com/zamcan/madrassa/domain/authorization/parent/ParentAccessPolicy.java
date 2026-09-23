package com.zamcan.madrassa.domain.authorization.parent;

import com.zamcan.madrassa.data.model.Parent;
import com.zamcan.madrassa.data.model.Student;

public final class ParentAccessPolicy {

    private ParentAccessPolicy() {
    }

    public static boolean canLogin(Parent parent) {
        return parent != null
                && parent.active
                && !isBlank(parent.id)
                && !isBlank(parent.madrassaId);
    }

    public static boolean sameMadrassa(
            Parent parent,
            Student student
    ) {
        if (parent == null || student == null) {
            return false;
        }

        if (isBlank(parent.madrassaId)
                || isBlank(student.madrassaId)) {
            return false;
        }

        return parent.madrassaId.trim()
                .equals(student.madrassaId.trim());
    }

    public static boolean canAccessStudent(
            Parent parent,
            Student student
    ) {
        if (!canLogin(parent)) {
            return false;
        }

        if (student == null || !student.active) {
            return false;
        }

        if (!sameMadrassa(parent, student)) {
            return false;
        }

        if (isBlank(parent.id)
                || isBlank(student.parentId)) {
            return false;
        }

        return parent.id.trim()
                .equals(student.parentId.trim());
    }

    public static boolean ownsStudent(
            Parent parent,
            String studentId
    ) {
        if (!canLogin(parent)
                || studentId == null
                || parent.linkedStudentIds == null) {
            return false;
        }

        String target = studentId.trim();

        if (target.isEmpty()) {
            return false;
        }

        for (String linked : parent.linkedStudentIds) {
            if (linked != null
                    && target.equals(linked.trim())) {
                return true;
            }
        }

        return false;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
