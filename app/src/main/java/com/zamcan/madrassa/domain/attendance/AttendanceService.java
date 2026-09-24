package com.zamcan.madrassa.domain.attendance;

import com.zamcan.madrassa.data.model.Attendance;
import com.zamcan.madrassa.data.model.AttendanceStatus;
import com.zamcan.madrassa.data.model.ClassGroup;
import com.zamcan.madrassa.data.model.Student;
import com.zamcan.madrassa.domain.common.IdGenerator;
import com.zamcan.madrassa.domain.common.OperationResult;
import com.zamcan.madrassa.domain.common.TenantPolicy;
import com.zamcan.madrassa.domain.repository.AttendanceStore;
import com.zamcan.madrassa.domain.repository.ClassStore;
import com.zamcan.madrassa.domain.repository.StudentStore;

import java.util.List;

public final class AttendanceService {
    private final AttendanceStore attendance;
    private final StudentStore students;
    private final ClassStore classes;
    private final String madrassaId;

    public AttendanceService(AttendanceStore attendance, StudentStore students,
                             ClassStore classes, String madrassaId) {
        if (attendance == null || students == null || classes == null || blank(madrassaId)) {
            throw new IllegalArgumentException("attendance dependencies and madrassaId are required");
        }
        this.attendance = attendance;
        this.students = students;
        this.classes = classes;
        this.madrassaId = madrassaId.trim();
    }

    public OperationResult<Attendance> record(Attendance value) {
        if (value == null || blank(value.studentId) || blank(value.classId)
                || blank(value.date) || value.status == null) {
            return invalid("attendance_invalid");
        }

        Student student = students.findById(value.studentId.trim());
        ClassGroup group = classes.findById(value.classId.trim());
        if (student == null) return missing("student_missing");
        if (group == null) return missing("class_missing");
        if (!student.active) return forbidden("student_inactive");
        if (!group.active) return forbidden("class_inactive");

        if (!TenantPolicy.sameMadrassa(madrassaId, student.madrassaId)
                || !TenantPolicy.sameMadrassa(madrassaId, group.madrassaId)) {
            return forbidden("attendance_tenant_mismatch");
        }

        String date = value.date.trim();
        List<Attendance> existing = attendance.findByClassAndDate(group.id, date);
        for (Attendance item : existing) {
            if (student.id.equals(item.studentId)) {
                return conflict("attendance_exists");
            }
        }

        value.id = blank(value.id) ? IdGenerator.newId() : value.id.trim();
        value.studentId = student.id;
        value.classId = group.id;
        value.date = date;
        value.recordedBy = blank(value.recordedBy) ? null : value.recordedBy.trim();

        return attendance.save(value)
                ? OperationResult.success(value)
                : failed("attendance_save_failed");
    }

    public OperationResult<Attendance> update(Attendance value) {
        if (value == null || blank(value.id) || blank(value.studentId)
                || blank(value.classId) || blank(value.date) || value.status == null) {
            return invalid("attendance_invalid");
        }

        Attendance existing = attendance.findById(value.id.trim());
        if (existing == null) return missing("attendance_missing");

        Student student = students.findById(value.studentId.trim());
        ClassGroup group = classes.findById(value.classId.trim());
        if (student == null || group == null) return missing("attendance_reference_missing");

        if (!TenantPolicy.sameMadrassa(madrassaId, student.madrassaId)
                || !TenantPolicy.sameMadrassa(madrassaId, group.madrassaId)
                || !TenantPolicy.sameMadrassa(madrassaId, student.madrassaId)) {
            return forbidden("attendance_tenant_mismatch");
        }

        value.id = existing.id;
        value.studentId = student.id;
        value.classId = group.id;
        value.date = value.date.trim();
        value.recordedBy = blank(value.recordedBy) ? existing.recordedBy : value.recordedBy.trim();

        return attendance.update(value)
                ? OperationResult.success(value)
                : failed("attendance_update_failed");
    }

    private static <T> OperationResult<T> invalid(String code) {
        return OperationResult.validationError(code, "Attendance data is invalid.");
    }
    private static <T> OperationResult<T> missing(String code) {
        return OperationResult.notFound(code, "Attendance record was not found.");
    }
    private static <T> OperationResult<T> conflict(String code) {
        return OperationResult.conflict(code, "Attendance already exists for this student and date.");
    }
    private static <T> OperationResult<T> forbidden(String code) {
        return OperationResult.forbidden(code, "Attendance operation is not authorized.");
    }
    private static <T> OperationResult<T> failed(String code) {
        return OperationResult.failure(code, "Attendance operation failed.");
    }
    private static boolean blank(String v) { return v == null || v.trim().isEmpty(); }
}
