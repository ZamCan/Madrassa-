package com.zamcan.madrassa.domain.academic;

import com.zamcan.madrassa.data.local.EduNoorDatabase;
import com.zamcan.madrassa.data.repository.AcademicUnitRepository;
import com.zamcan.madrassa.data.repository.AssessmentRepository;
import com.zamcan.madrassa.data.repository.AssignmentRepository;
import com.zamcan.madrassa.data.repository.CourseRepository;
import com.zamcan.madrassa.data.repository.LearningMaterialRepository;
import com.zamcan.madrassa.data.repository.LearningProgressRepository;
import com.zamcan.madrassa.data.repository.LessonRepository;
import com.zamcan.madrassa.data.repository.ProgrammeRepository;
import com.zamcan.madrassa.data.repository.StudentRepository;
import com.zamcan.madrassa.domain.authorization.AcademicAccessContext;

/**
 * Application composition boundary for the Academic Core.
 *
 * The caller must provide an explicit access context. This prevents
 * repository-backed academic services from silently operating with
 * an inferred or unbound tenant.
 */
public final class AcademicCoreServiceFactory {

    private AcademicCoreServiceFactory() {
    }

    public static AcademicCoreService forMadrassa(
            EduNoorDatabase database,
            String madrassaId
    ) {
        if (database == null) {
            throw new IllegalArgumentException("database is required");
        }

        if (blank(madrassaId)) {
            throw new IllegalArgumentException("madrassaId is required");
        }

        return create(
                database,
                AcademicAccessContext.forMadrassa(
                        madrassaId.trim()
                )
        );
    }

    public static AcademicCoreService forSolo(
            EduNoorDatabase database
    ) {
        if (database == null) {
            throw new IllegalArgumentException("database is required");
        }

        return create(
                database,
                AcademicAccessContext.forSolo()
        );
    }

    private static AcademicCoreService create(
            EduNoorDatabase database,
            AcademicAccessContext accessContext
    ) {
        ProgrammeRepository programmes =
                new ProgrammeRepository(database);

        return new AcademicCoreService(
                programmes,
                new CourseRepository(database),
                new AcademicUnitRepository(database),
                new LessonRepository(database),
                new LearningMaterialRepository(database),
                new AssignmentRepository(database),
                new AssessmentRepository(database),
                new LearningProgressRepository(database),
                new StudentRepository(database),
                accessContext
        );
    }

    private static boolean blank(String value) {
        return value == null
                || value.trim().isEmpty();
    }
}
