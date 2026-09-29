package com.zamcan.madrassa.domain.parent;

import com.zamcan.madrassa.data.model.Parent;
import com.zamcan.madrassa.data.model.Student;
import com.zamcan.madrassa.domain.authorization.MadrassaAccessContext;
import com.zamcan.madrassa.domain.common.OperationResult;
import com.zamcan.madrassa.domain.common.TenantPolicy;
import com.zamcan.madrassa.domain.repository.ParentStore;
import com.zamcan.madrassa.domain.repository.StudentStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Parent-facing read boundary.
 *
 * The parent ID is an account reference, never a tenant authorization
 * credential. The authenticated MadrassaAccessContext remains authoritative.
 */
public final class ParentStudentAccessService {
    private final ParentStore parents;
    private final StudentStore students;
    private final MadrassaAccessContext accessContext;

    public ParentStudentAccessService(
            ParentStore parents,
            StudentStore students,
            MadrassaAccessContext accessContext
    ) {
        if (parents == null || students == null || accessContext == null) {
            throw new IllegalArgumentException("parent access dependencies are required");
        }
        this.parents = parents;
        this.students = students;
        this.accessContext = accessContext;
    }

    public OperationResult<List<Student>> listChildren(String parentId) {
        Parent parent = requireParent(parentId);
        if (parent == null) {
            return OperationResult.notFound(
                    "parent_missing",
                    "Parent account was not found."
            );
        }

        if (!parent.active) {
            return OperationResult.forbidden(
                    "parent_inactive",
                    "Parent account is inactive."
            );
        }

        List<Student> result = new ArrayList<>();
        for (Student student : students.findByParent(parent.id)) {
            if (student == null || !student.active) {
                continue;
            }

            if (!accessContext.allows(student.madrassaId)
                    || !TenantPolicy.sameMadrassa(
                    parent.madrassaId,
                    student.madrassaId
            )) {
                continue;
            }

            result.add(student);
        }

        return OperationResult.success(Collections.unmodifiableList(result));
    }

    public OperationResult<Student> getChild(
            String parentId,
            String studentId
    ) {
        Parent parent = requireParent(parentId);
        if (parent == null) {
            return OperationResult.notFound(
                    "parent_missing",
                    "Parent account was not found."
            );
        }

        if (!parent.active) {
            return OperationResult.forbidden(
                    "parent_inactive",
                    "Parent account is inactive."
            );
        }

        if (isBlank(studentId)) {
            return OperationResult.validationError(
                    "student_id_required",
                    "Student ID is required."
            );
        }

        Student student = students.findById(studentId.trim());
        if (student == null) {
            return OperationResult.notFound(
                    "student_missing",
                    "Student was not found."
            );
        }

        if (!student.active) {
            return OperationResult.forbidden(
                    "student_inactive",
                    "Student is inactive."
            );
        }

        boolean linked = parent.id.equals(student.parentId)
                || contains(parent.linkedStudentIds, student.id);

        if (!linked) {
            return OperationResult.forbidden(
                    "parent_student_not_linked",
                    "This student is not linked to the parent account."
            );
        }

        if (!accessContext.allows(parent.madrassaId)
                || !accessContext.allows(student.madrassaId)
                || !TenantPolicy.sameMadrassa(
                parent.madrassaId,
                student.madrassaId
        )) {
            return OperationResult.forbidden(
                    "parent_student_tenant_mismatch",
                    "Parent and student are outside the authorized Madrassa."
            );
        }

        return OperationResult.success(student);
    }

    private Parent requireParent(String parentId) {
        if (isBlank(parentId)) {
            return null;
        }
        return parents.findById(parentId.trim());
    }

    private static boolean contains(List<String> values, String target) {
        if (values == null || isBlank(target)) {
            return false;
        }

        for (String value : values) {
            if (!isBlank(value) && target.equals(value.trim())) {
                return true;
            }
        }
        return false;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
