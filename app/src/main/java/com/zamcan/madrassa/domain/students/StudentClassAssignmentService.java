package com.zamcan.madrassa.domain.students;

import com.zamcan.madrassa.data.model.ClassGroup;
import com.zamcan.madrassa.data.model.Student;
import com.zamcan.madrassa.domain.authorization.MadrassaAccessContext;
import com.zamcan.madrassa.domain.common.OperationResult;
import com.zamcan.madrassa.domain.common.TenantPolicy;
import com.zamcan.madrassa.domain.repository.ClassStore;
import com.zamcan.madrassa.domain.repository.StudentStore;

/** Tenant-scoped class placement without implicit promotion rules. */
public final class StudentClassAssignmentService {

    private final StudentStore students;
    private final ClassStore classes;
    private final MadrassaAccessContext accessContext;

    public StudentClassAssignmentService(
            StudentStore students,
            ClassStore classes,
            MadrassaAccessContext accessContext
    ) {
        if (students == null
                || classes == null
                || accessContext == null) {
            throw new IllegalArgumentException(
                    "class assignment dependencies are required"
            );
        }

        this.students = students;
        this.classes = classes;
        this.accessContext = accessContext;
    }

    public OperationResult<Student> assign(
            String studentId,
            String classId
    ) {
        if (blank(studentId) || blank(classId)) {
            return OperationResult.validationError(
                    "class_assignment_invalid",
                    "Student and class are required."
            );
        }

        Student student = students.findById(studentId.trim());
        ClassGroup target = classes.findById(classId.trim());

        if (student == null) {
            return OperationResult.notFound(
                    "student_missing",
                    "Student was not found."
            );
        }

        if (target == null) {
            return OperationResult.notFound(
                    "class_missing",
                    "Class was not found."
            );
        }

        if (!student.active) {
            return OperationResult.forbidden(
                    "student_inactive",
                    "Inactive students cannot be placed into a class."
            );
        }

        if (!target.active) {
            return OperationResult.forbidden(
                    "class_inactive",
                    "Students cannot be placed into an inactive class."
            );
        }

        if (!accessContext.allows(student.madrassaId)
                || !accessContext.allows(target.madrassaId)
                || !TenantPolicy.sameMadrassa(
                student.madrassaId,
                target.madrassaId
        )) {
            return OperationResult.forbidden(
                    "class_assignment_tenant_mismatch",
                    "Student and class must belong to the authorized Madrassa."
            );
        }

        if (target.id.equals(student.classId)) {
            return OperationResult.success(student);
        }

        student.classId = target.id;

        try {
            students.update(student);
            return OperationResult.success(student);
        } catch (RuntimeException error) {
            return OperationResult.failed(
                    "class_assignment_failed",
                    "Student class assignment could not be saved."
            );
        }
    }

    private static boolean blank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
