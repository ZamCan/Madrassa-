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
 * Composition boundary for the Academic Core.
 *
 * <p>Production callers must choose a tenant or Solo scope explicitly.
 * The factory keeps repository construction in one place and prevents an
 * unbound service from being used as a tenant authorization decision.</p>
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

        return create(
                database,
                AcademicAccessContext.forMadrassa(madrassaId)
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
        return new AcademicCoreService(
                new ProgrammeRepository(database),
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
}
