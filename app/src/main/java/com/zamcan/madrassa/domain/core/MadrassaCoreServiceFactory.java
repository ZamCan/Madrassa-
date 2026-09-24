package com.zamcan.madrassa.domain.core;

import com.zamcan.madrassa.data.local.EduNoorDatabase;
import com.zamcan.madrassa.data.repository.ClassRepository;
import com.zamcan.madrassa.data.repository.ParentRepository;
import com.zamcan.madrassa.data.repository.ParentStudentLinkRepository;
import com.zamcan.madrassa.data.repository.ProgrammeRepository;
import com.zamcan.madrassa.data.repository.StudentAdmissionRepository;
import com.zamcan.madrassa.data.repository.StudentRepository;
import com.zamcan.madrassa.domain.academic.ClassManagementService;
import com.zamcan.madrassa.domain.authorization.MadrassaAccessContext;
import com.zamcan.madrassa.domain.authorization.parent.ParentStudentLinkService;
import com.zamcan.madrassa.domain.repository.ClassStore;
import com.zamcan.madrassa.domain.repository.ParentStore;
import com.zamcan.madrassa.domain.repository.ProgrammeStore;
import com.zamcan.madrassa.domain.repository.StudentAdmissionStore;
import com.zamcan.madrassa.domain.repository.StudentStore;
import com.zamcan.madrassa.domain.students.StudentAdmissionService;
import com.zamcan.madrassa.domain.students.StudentProgrammeEnrollmentService;

public final class MadrassaCoreServiceFactory {
    private MadrassaCoreServiceFactory() {}

    public static StudentProgrammeEnrollmentService enrolment(
            EduNoorDatabase database, String madrassaId) {
        require(database, madrassaId);
        MadrassaAccessContext scope = MadrassaAccessContext.forMadrassa(madrassaId);
        return new StudentProgrammeEnrollmentService(
                new StudentRepository(database),
                new ProgrammeRepository(database),
                scope);
    }

    public static StudentAdmissionService admission(
            EduNoorDatabase database, String madrassaId) {
        require(database, madrassaId);
        MadrassaAccessContext scope = MadrassaAccessContext.forMadrassa(madrassaId);
        return new StudentAdmissionService(
                new ParentRepository(database),
                new StudentRepository(database),
                new ClassRepository(database),
                new ProgrammeRepository(database),
                new StudentAdmissionRepository(database),
                scope);
    }

    public static ParentStudentLinkService parentStudentLinks(
            EduNoorDatabase database, String madrassaId) {
        require(database, madrassaId);
        MadrassaAccessContext scope = MadrassaAccessContext.forMadrassa(madrassaId);
        return new ParentStudentLinkService(
                new ParentRepository(database),
                new StudentRepository(database),
                new ParentStudentLinkRepository(database),
                scope);
    }

    public static ClassManagementService classes(
            EduNoorDatabase database, String madrassaId) {
        require(database, madrassaId);
        return new ClassManagementService(
                new ClassRepository(database),
                MadrassaAccessContext.forMadrassa(madrassaId));
    }

    private static void require(EduNoorDatabase database, String madrassaId) {
        if (database == null) throw new IllegalArgumentException("database is required");
        if (madrassaId == null || madrassaId.trim().isEmpty()) {
            throw new IllegalArgumentException("madrassaId is required");
        }
    }
}
