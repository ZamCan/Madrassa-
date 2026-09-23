package com.zamcan.madrassa.domain.students;

import com.zamcan.madrassa.data.model.ClassGroup;
import com.zamcan.madrassa.data.model.Parent;
import com.zamcan.madrassa.data.model.Programme;
import com.zamcan.madrassa.data.model.Student;
import com.zamcan.madrassa.domain.common.TenantPolicy;
import com.zamcan.madrassa.domain.repository.ClassStore;
import com.zamcan.madrassa.domain.repository.ParentStore;
import com.zamcan.madrassa.domain.repository.ProgrammeStore;
import com.zamcan.madrassa.domain.repository.StudentAdmissionStore;
import com.zamcan.madrassa.domain.repository.StudentStore;

public final class StudentAdmissionService {

    private final ParentStore parentStore;
    private final StudentStore studentStore;
    private final ClassStore classStore;
    private final ProgrammeStore programmeStore;
    private final StudentAdmissionStore admissionStore;

    public StudentAdmissionService(
            ParentStore parentStore,
            StudentStore studentStore,
            ClassStore classStore,
            ProgrammeStore programmeStore,
            StudentAdmissionStore admissionStore
    ) {
        this.parentStore = parentStore;
        this.studentStore = studentStore;
        this.classStore = classStore;
        this.programmeStore = programmeStore;
        this.admissionStore = admissionStore;
    }

    public void admit(
            Student student,
            String parentId
    ) {
        validateInput(student, parentId);

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

        TenantPolicy.requireSameMadrassa(
                student.madrassaId,
                parent.madrassaId
        );

        if (studentStore.findById(
                student.id.trim()
        ) != null) {
            throw new IllegalStateException(
                    "Student already exists."
            );
        }

        ClassGroup classGroup =
                classStore.findById(
                        student.classId.trim()
                );

        if (classGroup == null) {
            throw new IllegalArgumentException(
                    "Class not found."
            );
        }

        if (!classGroup.active) {
            throw new SecurityException(
                    "Class is inactive."
            );
        }

        TenantPolicy.requireSameMadrassa(
                student.madrassaId,
                classGroup.madrassaId
        );

        if (student.programmeIds != null) {
            for (String programmeId :
                    student.programmeIds) {

                if (isBlank(programmeId)) {
                    throw new IllegalArgumentException(
                            "Programme ID cannot be blank."
                    );
                }

                Programme programme =
                        programmeStore.findById(
                                programmeId.trim()
                        );

                if (programme == null) {
                    throw new IllegalArgumentException(
                            "Programme not found: " +
                                    programmeId
                    );
                }

                if (!programme.active) {
                    throw new SecurityException(
                            "Programme is inactive: " +
                                    programme.name
                    );
                }

                TenantPolicy.requireSameMadrassa(
                        student.madrassaId,
                        programme.madrassaId
                );
            }
        }

        student.parentId =
                parent.id.trim();

        admissionStore.admit(
                student,
                parent.id.trim()
        );
    }

    private void validateInput(
            Student student,
            String parentId
    ) {
        if (student == null) {
            throw new IllegalArgumentException(
                    "student is required."
            );
        }

        if (isBlank(parentId)) {
            throw new IllegalArgumentException(
                    "parentId is required."
            );
        }

        if (isBlank(student.id)
                || isBlank(student.madrassaId)
                || isBlank(student.fullName)
                || isBlank(student.parentGuardianName)
                || isBlank(student.mainPhone)
                || isBlank(student.classId)
                || isBlank(student.quranLevel)
                || isBlank(student.hifzLevel)) {

            throw new IllegalArgumentException(
                    "Student admission data is incomplete."
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
