package com.zamcan.madrassa.domain.students;

import com.zamcan.madrassa.data.model.Programme;
import com.zamcan.madrassa.data.model.Student;
import com.zamcan.madrassa.domain.common.TenantPolicy;
import com.zamcan.madrassa.domain.authorization.MadrassaAccessContext;
import com.zamcan.madrassa.data.local.DatabaseTransactionRunner;
import com.zamcan.madrassa.data.local.EduNoorDatabase;
import com.zamcan.madrassa.domain.repository.ProgrammeStore;
import com.zamcan.madrassa.domain.repository.StudentStore;

public final class StudentProgrammeEnrollmentService {

    private final StudentStore studentStore;
    private final ProgrammeStore programmeStore;
    private final MadrassaAccessContext accessContext;
    private final DatabaseTransactionRunner transactions;

    public StudentProgrammeEnrollmentService(
            StudentStore studentStore,
            ProgrammeStore programmeStore,
            MadrassaAccessContext accessContext
    ) {
        if (studentStore == null || programmeStore == null || accessContext == null) {
            throw new IllegalArgumentException("enrollment dependencies are required");
        }
        this.studentStore = studentStore;
        this.programmeStore = programmeStore;
        this.accessContext = accessContext;
        this.transactions = null;
    }

    public void enroll(
            String studentId,
            String programmeId
    ) {
        Student student = requireStudent(studentId);

        Programme programme = requireProgramme(programmeId);

        requireActive(student, programme);
        requireTenant(student, programme);

        requireTenant(student, programme);

        if (student.programmeIds == null) {
            throw new IllegalStateException(
                    "Student programme collection is unavailable."
            );
        }

        if (!student.programmeIds.contains(programme.id)) {
            student.programmeIds.add(
                    programme.id
            );

            studentStore.update(student);
        }
    }

    public void remove(
            String studentId,
            String programmeId
    ) {
        Student student =
                requireStudent(studentId);

        Programme programme =
                requireProgramme(programmeId);

        TenantPolicy.requireSameMadrassa(
                student.madrassaId,
                programme.madrassaId
        );

        if (student.programmeIds == null) {
            return;
        }

        if (student.programmeIds.remove(
                programme.id
        )) {
            studentStore.update(student);
        }
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

    private Programme requireProgramme(
            String programmeId
    ) {
        if (isBlank(programmeId)) {
            throw new IllegalArgumentException(
                    "programmeId is required."
            );
        }

        Programme programme =
                programmeStore.findById(
                        programmeId.trim()
                );

        if (programme == null) {
            throw new IllegalArgumentException(
                    "Programme not found."
            );
        }

        if (!programme.active) {
            throw new SecurityException(
                    "Programme is inactive."
            );
        }

        return programme;
    }

    private void requireTenant(Student student, Programme programme) {
        if (!accessContext.allows(student.madrassaId)
                || !accessContext.allows(programme.madrassaId)) {
            throw new SecurityException("Enrollment is outside the authorized Madrassa.");
        }
        TenantPolicy.requireSameMadrassa(student.madrassaId, programme.madrassaId);
    }

    private void requireActive(
            Student student,
            Programme programme
    ) {
        if (!student.active
                || !programme.active) {
            throw new SecurityException(
                    "Inactive records cannot be enrolled."
            );
        }
    }

    private static boolean isBlank(
            String value
    ) {
        return value == null
                || value.trim().isEmpty();
    }
}
