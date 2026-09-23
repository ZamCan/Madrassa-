package com.zamcan.madrassa.domain.academic;

import com.zamcan.madrassa.data.model.AcademicUnit;
import com.zamcan.madrassa.data.model.Assessment;
import com.zamcan.madrassa.data.model.Assignment;
import com.zamcan.madrassa.data.model.Course;
import com.zamcan.madrassa.data.model.LearningMaterial;
import com.zamcan.madrassa.data.model.LearningProgress;
import com.zamcan.madrassa.data.model.Lesson;
import com.zamcan.madrassa.data.model.Programme;
import com.zamcan.madrassa.data.model.Student;
import com.zamcan.madrassa.domain.common.OperationResult;
import com.zamcan.madrassa.domain.common.TenantPolicy;
import com.zamcan.madrassa.domain.authorization.AcademicAccessContext;
import com.zamcan.madrassa.domain.repository.AcademicUnitStore;
import com.zamcan.madrassa.domain.repository.AssessmentStore;
import com.zamcan.madrassa.domain.repository.AssignmentStore;
import com.zamcan.madrassa.domain.repository.CourseStore;
import com.zamcan.madrassa.domain.repository.LearningMaterialStore;
import com.zamcan.madrassa.domain.repository.LearningProgressStore;
import com.zamcan.madrassa.domain.repository.LessonStore;
import com.zamcan.madrassa.domain.repository.ProgrammeStore;
import com.zamcan.madrassa.domain.repository.StudentStore;

import java.util.List;

/**
 * Academic Core use cases.
 *
 * Persistence stays behind repository contracts.
 * This service enforces academic hierarchy, tenant ownership,
 * programme enrollment and the local-only Solo boundary.
 */
public final class AcademicCoreService {

    private final ProgrammeStore programmes;
    private final CourseStore courses;
    private final AcademicUnitStore units;
    private final LessonStore lessons;
    private final LearningMaterialStore materials;
    private final AssignmentStore assignments;
    private final AssessmentStore assessments;
    private final LearningProgressStore progress;
    private final StudentStore students;
    private final AcademicAccessContext accessContext;

    /**
     * Compatibility constructor. It intentionally creates an unbound service:
     * Madrassa/solo writes require the explicit access-context constructor.
     */
    @Deprecated
    public AcademicCoreService(
            ProgrammeStore programmes,
            CourseStore courses,
            AcademicUnitStore units,
            LessonStore lessons,
            LearningMaterialStore materials,
            AssignmentStore assignments,
            AssessmentStore assessments,
            LearningProgressStore progress,
            StudentStore students
    ) {
        this(
                programmes,
                courses,
                units,
                lessons,
                materials,
                assignments,
                assessments,
                progress,
                students,
                null
        );
    }

    /**
     * Creates an academic service bound to one explicit tenant scope.
     *
     * Use AcademicAccessContext.forMadrassa(...) for Madrassa work or
     * AcademicAccessContext.forSolo() for global/local-only Solo work.
     */
    public AcademicCoreService(
            ProgrammeStore programmes,
            CourseStore courses,
            AcademicUnitStore units,
            LessonStore lessons,
            LearningMaterialStore materials,
            AssignmentStore assignments,
            AssessmentStore assessments,
            LearningProgressStore progress,
            StudentStore students,
            AcademicAccessContext accessContext
    ) {
        if (programmes == null
                || courses == null
                || units == null
                || lessons == null
                || materials == null
                || assignments == null
                || assessments == null
                || progress == null
                || students == null) {
            throw new IllegalArgumentException(
                    "academic dependencies are required"
            );
        }

        this.programmes = programmes;
        this.courses = courses;
        this.units = units;
        this.lessons = lessons;
        this.materials = materials;
        this.assignments = assignments;
        this.assessments = assessments;
        this.progress = progress;
        this.students = students;
        this.accessContext = accessContext;
    }

    /**
     * Creates a course under either:
     *
     * 1. a Madrassa-owned programme, or
     * 2. a global programme used by Solo and other authorized
     *    EduNoor experiences.
     *
     * A global course is represented by madrassaId == null.
     */
    public OperationResult<Course> createCourse(Course value) {
        if (!authorizedScope(value.madrassaId)) {
            return forbidden("academic_scope_not_authorized");
        }

        if (value == null
                || blank(value.id)
                || blank(value.programmeId)
                || blank(value.name)) {
            return invalid("course_invalid");
        }

        String programmeId = value.programmeId.trim();
        String name = value.name.trim();

        Programme programme = programmes.findById(programmeId);

        if (programme == null) {
            return missing("programme_missing");
        }

        if (!programme.active) {
            return forbidden("programme_inactive");
        }

        boolean global = blank(value.madrassaId);

        if (global) {
            if (!blank(programme.madrassaId)) {
                return forbidden("global_course_requires_global_programme");
            }
        } else {
            String madrassaId = value.madrassaId.trim();

            if (!TenantPolicy.sameMadrassa(
                    madrassaId,
                    programme.madrassaId
            )) {
                return forbidden("course_tenant_mismatch");
            }

            value.madrassaId = madrassaId;
        }

        for (Course existing : courses.findByProgramme(programmeId)) {
            if (existing != null
                    && existing.name != null
                    && name.equalsIgnoreCase(existing.name.trim())) {
                return conflict("course_exists");
            }
        }

        value.id = value.id.trim();
        value.madrassaId = global ? null : value.madrassaId;
        value.programmeId = programmeId;
        value.name = name;

        return courses.save(value)
                ? OperationResult.success(value)
                : failed("course_save_failed");
    }

    public OperationResult<AcademicUnit> createUnit(
            AcademicUnit value
    ) {
        if (!authorizedScope(value.madrassaId)) {
            return forbidden("academic_scope_not_authorized");
        }

        if (value == null
                || blank(value.id)
                || blank(value.courseId)
                || blank(value.name)) {
            return invalid("unit_invalid");
        }

        String courseId = value.courseId.trim();

        Course course = courses.findById(courseId);

        if (course == null) {
            return missing("course_missing");
        }

        if (!course.active) {
            return forbidden("course_inactive");
        }

        Programme programme = programmes.findById(
                course.programmeId
        );

        if (programme == null) {
            return missing("programme_missing");
        }

        if (!programme.active) {
            return forbidden("programme_inactive");
        }

        boolean global = blank(value.madrassaId);

        if (global) {
            if (!blank(course.madrassaId)
                    || !blank(programme.madrassaId)) {
                return forbidden("global_unit_requires_global_hierarchy");
            }
        } else {
            String madrassaId = value.madrassaId.trim();

            if (!TenantPolicy.sameMadrassa(
                    madrassaId,
                    course.madrassaId
            )) {
                return forbidden("unit_tenant_mismatch");
            }

            if (!TenantPolicy.sameMadrassa(
                    madrassaId,
                    programme.madrassaId
            )) {
                return forbidden("unit_programme_tenant_mismatch");
            }

            value.madrassaId = madrassaId;
        }

        String name = value.name.trim();

        for (AcademicUnit existing : units.findByCourse(courseId)) {
            if (existing != null
                    && existing.name != null
                    && name.equalsIgnoreCase(existing.name.trim())) {
                return conflict("unit_exists");
            }
        }

        value.id = value.id.trim();
        value.madrassaId = global ? null : value.madrassaId;
        value.courseId = courseId;
        value.name = name;

        return units.save(value)
                ? OperationResult.success(value)
                : failed("unit_save_failed");
    }

    /**
     * Lessons may be Madrassa-owned or global/Solo-owned.
     *
     * Global lessons are represented by a blank madrassaId.
     * For a Madrassa-owned lesson, the unit/course/programme chain
     * must belong to the same Madrassa.
     */
    public OperationResult<Lesson> createLesson(
            Lesson value
    ) {
        if (!authorizedScope(value.madrassaId)) {
            return forbidden("academic_scope_not_authorized");
        }

        if (value == null
                || blank(value.id)
                || blank(value.unitId)
                || blank(value.name)
                || blank(value.contentType)) {
            return invalid("lesson_invalid");
        }

        String unitId = value.unitId.trim();

        AcademicUnit unit = units.findById(unitId);

        if (unit == null) {
            return missing("unit_missing");
        }

        Course course = courses.findById(unit.courseId);

        if (course == null) {
            return missing("course_missing");
        }

        if (!course.active) {
            return forbidden("course_inactive");
        }

        Programme programme = programmes.findById(
                course.programmeId
        );

        if (programme == null) {
            return missing("programme_missing");
        }

        if (!programme.active) {
            return forbidden("programme_inactive");
        }

        boolean globalLesson = blank(value.madrassaId);

        if (globalLesson) {
            /*
             * A global lesson is allowed only when its academic
             * hierarchy is also not tied to a specific Madrassa.
             */
            if (!blank(unit.madrassaId)
                    || !blank(course.madrassaId)
                    || !blank(programme.madrassaId)) {
                return forbidden("global_lesson_requires_global_hierarchy");
            }

            value.madrassaId = null;
        } else {
            String madrassaId = value.madrassaId.trim();

            if (!TenantPolicy.sameMadrassa(
                    madrassaId,
                    unit.madrassaId
            )) {
                return forbidden("lesson_unit_tenant_mismatch");
            }

            if (!TenantPolicy.sameMadrassa(
                    madrassaId,
                    course.madrassaId
            )) {
                return forbidden("lesson_course_tenant_mismatch");
            }

            if (!TenantPolicy.sameMadrassa(
                    madrassaId,
                    programme.madrassaId
            )) {
                return forbidden("lesson_programme_tenant_mismatch");
            }

            value.madrassaId = madrassaId;
        }

        value.id = value.id.trim();
        value.unitId = unitId;
        value.name = value.name.trim();
        value.contentType = value.contentType.trim();

        return lessons.save(value)
                ? OperationResult.success(value)
                : failed("lesson_save_failed");
    }

    /**
     * Learning materials are programme-owned in the current schema.
     * They therefore remain Madrassa-owned rather than global Solo
     * records until a future schema explicitly introduces global
     * programme/content ownership.
     */
    /**
     * Learning materials may be global or Madrassa-owned.
     *
     * Global materials belong to a global programme and are available
     * to Solo and other authorized EduNoor experiences.
     *
     * Madrassa materials remain tenant-isolated.
     */
    public OperationResult<LearningMaterial> createMaterial(
            LearningMaterial value
    ) {
        if (!authorizedScope(value.madrassaId)) {
            return forbidden("academic_scope_not_authorized");
        }

        if (value == null
                || blank(value.id)
                || blank(value.programmeId)
                || blank(value.title)
                || blank(value.type)
                || value.content == null) {
            return invalid("material_invalid");
        }

        String programmeId = value.programmeId.trim();

        Programme programme = programmes.findById(programmeId);

        if (programme == null) {
            return missing("programme_missing");
        }

        if (!programme.active) {
            return forbidden("programme_inactive");
        }

        boolean global = blank(value.madrassaId);

        if (global) {
            if (!blank(programme.madrassaId)) {
                return forbidden("global_material_requires_global_programme");
            }
        } else {
            String madrassaId = value.madrassaId.trim();

            if (!TenantPolicy.sameMadrassa(
                    madrassaId,
                    programme.madrassaId
            )) {
                return forbidden("material_tenant_mismatch");
            }

            value.madrassaId = madrassaId;
        }

        value.id = value.id.trim();
        value.madrassaId = global ? null : value.madrassaId;
        value.programmeId = programmeId;
        value.title = value.title.trim();
        value.type = value.type.trim();

        return materials.save(value)
                ? OperationResult.success(value)
                : failed("material_save_failed");
    }

    public OperationResult<Assignment> createAssignment(
            Assignment value
    ) {
        if (!authorizedScope(value.madrassaId)) {
            return forbidden("academic_scope_not_authorized");
        }

        if (value == null
                || blank(value.id)
                || blank(value.programmeId)
                || blank(value.title)
                || value.maxPoints < 0) {
            return invalid("assignment_invalid");
        }

        String programmeId = value.programmeId.trim();

        Programme programme = programmes.findById(programmeId);

        if (programme == null) {
            return missing("programme_missing");
        }

        if (!programme.active) {
            return forbidden("programme_inactive");
        }

        boolean global = blank(value.madrassaId);

        if (global) {
            if (!blank(programme.madrassaId)) {
                return forbidden("global_assignment_requires_global_programme");
            }
        } else {
            String madrassaId = value.madrassaId.trim();

            if (!TenantPolicy.sameMadrassa(
                    madrassaId,
                    programme.madrassaId
            )) {
                return forbidden("assignment_tenant_mismatch");
            }

            value.madrassaId = madrassaId;
        }

        Course course = null;
        AcademicUnit unit = null;
        Lesson lesson = null;

        if (!blank(value.courseId)) {
            String courseId = value.courseId.trim();

            course = courses.findById(courseId);

            if (course == null) {
                return missing("assignment_course_missing");
            }

            if (!course.active) {
                return forbidden("assignment_course_inactive");
            }

            if (!programmeId.equals(course.programmeId)) {
                return forbidden("assignment_course_programme_mismatch");
            }

            if (global) {
                if (!blank(course.madrassaId)) {
                    return forbidden("global_assignment_course_mismatch");
                }
            } else if (!TenantPolicy.sameMadrassa(
                    value.madrassaId,
                    course.madrassaId
            )) {
                return forbidden("assignment_course_tenant_mismatch");
            }

            value.courseId = courseId;
        }

        if (!blank(value.unitId)) {
            String unitId = value.unitId.trim();

            unit = units.findById(unitId);

            if (unit == null) {
                return missing("assignment_unit_missing");
            }

            course = courses.findById(unit.courseId);

            if (course == null) {
                return missing("assignment_unit_course_missing");
            }

            if (!course.active) {
                return forbidden("assignment_unit_course_inactive");
            }

            if (!programmeId.equals(course.programmeId)) {
                return forbidden("assignment_unit_programme_mismatch");
            }

            if (global) {
                if (!blank(unit.madrassaId)
                        || !blank(course.madrassaId)) {
                    return forbidden("global_assignment_unit_mismatch");
                }
            } else {
                if (!TenantPolicy.sameMadrassa(
                        value.madrassaId,
                        unit.madrassaId
                )) {
                    return forbidden("assignment_unit_tenant_mismatch");
                }

                if (!TenantPolicy.sameMadrassa(
                        value.madrassaId,
                        course.madrassaId
                )) {
                    return forbidden("assignment_unit_course_tenant_mismatch");
                }
            }

            if (!blank(value.courseId)
                    && !value.courseId.equals(unit.courseId)) {
                return forbidden("assignment_unit_course_mismatch");
            }

            value.unitId = unitId;
            value.courseId = course.id;
        }

        if (!blank(value.lessonId)) {
            String lessonId = value.lessonId.trim();

            lesson = lessons.findById(lessonId);

            if (lesson == null) {
                return missing("assignment_lesson_missing");
            }

            if (!lesson.active) {
                return forbidden("assignment_lesson_inactive");
            }

            unit = units.findById(lesson.unitId);

            if (unit == null) {
                return missing("assignment_lesson_unit_missing");
            }

            course = courses.findById(unit.courseId);

            if (course == null) {
                return missing("assignment_lesson_course_missing");
            }

            if (!course.active) {
                return forbidden("assignment_lesson_course_inactive");
            }

            if (!programmeId.equals(course.programmeId)) {
                return forbidden("assignment_lesson_programme_mismatch");
            }

            if (global) {
                if (!blank(lesson.madrassaId)
                        || !blank(unit.madrassaId)
                        || !blank(course.madrassaId)) {
                    return forbidden("global_assignment_lesson_mismatch");
                }
            } else {
                if (!TenantPolicy.sameMadrassa(
                        value.madrassaId,
                        lesson.madrassaId
                )) {
                    return forbidden("assignment_lesson_tenant_mismatch");
                }

                if (!TenantPolicy.sameMadrassa(
                        value.madrassaId,
                        unit.madrassaId
                )) {
                    return forbidden("assignment_lesson_unit_tenant_mismatch");
                }

                if (!TenantPolicy.sameMadrassa(
                        value.madrassaId,
                        course.madrassaId
                )) {
                    return forbidden("assignment_lesson_course_tenant_mismatch");
                }
            }

            if (!blank(value.unitId)
                    && !value.unitId.equals(lesson.unitId)) {
                return forbidden("assignment_lesson_unit_mismatch");
            }

            if (!blank(value.courseId)
                    && !value.courseId.equals(course.id)) {
                return forbidden("assignment_lesson_course_mismatch");
            }

            value.lessonId = lessonId;
            value.unitId = unit.id;
            value.courseId = course.id;
        }

        value.id = value.id.trim();
        value.madrassaId = global ? null : value.madrassaId;
        value.programmeId = programmeId;
        value.title = value.title.trim();

        return assignments.save(value)
                ? OperationResult.success(value)
                : failed("assignment_save_failed");
    }

    public OperationResult<Assessment> recordAssessment(
            Assessment value
    ) {
        if (!authorizedScope(value.madrassaId)) {
            return forbidden("academic_scope_not_authorized");
        }

        if (value == null
                || blank(value.id)
                || blank(value.madrassaId)
                || blank(value.studentId)
                || blank(value.assignmentId)
                || value.status == null
                || value.score < 0) {
            return invalid("assessment_invalid");
        }

        String madrassaId = value.madrassaId.trim();
        String studentId = value.studentId.trim();
        String assignmentId = value.assignmentId.trim();

        Student student = students.findById(studentId);
        Assignment assignment = assignments.findById(assignmentId);

        if (student == null) {
            return missing("student_missing");
        }

        if (assignment == null) {
            return missing("assignment_missing");
        }

        if (!student.active) {
            return forbidden("student_inactive");
        }

        if (!assignment.active) {
            return forbidden("assignment_inactive");
        }

        if (!TenantPolicy.sameMadrassa(
                madrassaId,
                student.madrassaId
        )) {
            return forbidden("assessment_student_tenant_mismatch");
        }

        if (blank(assignment.madrassaId)) {
            return forbidden("global_assignment_not_assessable_by_madrassa_student");
        }

        if (!TenantPolicy.sameMadrassa(
                madrassaId,
                assignment.madrassaId
        )) {
            return forbidden("assessment_assignment_tenant_mismatch");
        }

        if (value.score > assignment.maxPoints) {
            return invalid("assessment_score_exceeds_maximum");
        }

        String programmeId = blank(value.programmeId)
                ? assignment.programmeId
                : value.programmeId.trim();

        if (blank(programmeId)) {
            return invalid("assessment_programme_required");
        }

        if (!programmeId.equals(assignment.programmeId)) {
            return forbidden("assessment_programme_mismatch");
        }

        if (!isEnrolled(student, programmeId)) {
            return forbidden("student_not_enrolled_in_programme");
        }

        value.id = value.id.trim();
        value.madrassaId = madrassaId;
        value.studentId = studentId;
        value.assignmentId = assignmentId;
        value.programmeId = programmeId;
        value.recordedAt =
                value.recordedAt <= 0
                        ? System.currentTimeMillis()
                        : value.recordedAt;

        return assessments.save(value)
                ? OperationResult.success(value)
                : failed("assessment_save_failed");
    }

    public OperationResult<LearningProgress> recordStudentProgress(
            LearningProgress value
    ) {
        if (!authorizedScope(value.madrassaId)) {
            return forbidden("academic_scope_not_authorized");
        }

        if (value == null
                || blank(value.id)
                || blank(value.learnerId)
                || blank(value.lessonId)
                || blank(value.madrassaId)
                || value.status == null) {
            return invalid("student_progress_invalid");
        }

        String madrassaId = value.madrassaId.trim();
        String learnerId = value.learnerId.trim();
        String lessonId = value.lessonId.trim();

        Student student = students.findById(learnerId);
        Lesson lesson = lessons.findById(lessonId);

        if (student == null) {
            return missing("student_missing");
        }

        if (lesson == null) {
            return missing("lesson_missing");
        }

        if (!student.active) {
            return forbidden("student_inactive");
        }

        if (!lesson.active) {
            return forbidden("lesson_inactive");
        }

        if (!TenantPolicy.sameMadrassa(
                madrassaId,
                student.madrassaId
        )) {
            return forbidden("progress_student_tenant_mismatch");
        }

        if (blank(lesson.madrassaId)) {
            return forbidden("global_lesson_not_madrassa_student_content");
        }

        if (!TenantPolicy.sameMadrassa(
                madrassaId,
                lesson.madrassaId
        )) {
            return forbidden("progress_lesson_tenant_mismatch");
        }

        AcademicUnit unit = units.findById(lesson.unitId);

        if (unit == null) {
            return missing("progress_unit_missing");
        }

        Course course = courses.findById(unit.courseId);

        if (course == null) {
            return missing("progress_course_missing");
        }

        if (!course.active) {
            return forbidden("progress_course_inactive");
        }

        Programme programme = programmes.findById(
                course.programmeId
        );

        if (programme == null) {
            return missing("progress_programme_missing");
        }

        if (!programme.active) {
            return forbidden("progress_programme_inactive");
        }

        if (!TenantPolicy.sameMadrassa(
                madrassaId,
                programme.madrassaId
        )) {
            return forbidden("progress_programme_tenant_mismatch");
        }

        if (!isEnrolled(
                student,
                programme.id
        )) {
            return forbidden("student_not_enrolled_in_programme");
        }

        value.id = value.id.trim();
        value.madrassaId = madrassaId;
        value.learnerId = learnerId;
        value.lessonId = lessonId;
        value.updatedAt = System.currentTimeMillis();

        LearningProgress existing =
                progress.findByLearnerAndLesson(
                        learnerId,
                        lessonId
                );

        if (existing != null) {
            existing.madrassaId = madrassaId;
            existing.status = value.status;
            existing.updatedAt = value.updatedAt;

            return progress.update(existing)
                    ? OperationResult.success(existing)
                    : failed("progress_update_failed");
        }

        return progress.save(value)
                ? OperationResult.success(value)
                : failed("progress_save_failed");
    }

    /**
     * Solo progress is local-only.
     *
     * A Solo learner has no Madrassa tenant. Therefore the referenced
     * lesson must also be global-owned. The repository receives a null
     * madrassaId so the local record remains outside Madrassa tenancy.
     */
    public OperationResult<LearningProgress> recordSoloProgress(
            LearningProgress value
    ) {
        if (!authorizedScope(value.madrassaId)) {
            return forbidden("academic_scope_not_authorized");
        }

        if (value == null
                || blank(value.id)
                || blank(value.learnerId)
                || blank(value.lessonId)
                || !blank(value.madrassaId)
                || value.status == null) {
            return invalid("solo_progress_invalid");
        }

        String lessonId = value.lessonId.trim();

        Lesson lesson = lessons.findById(lessonId);

        if (lesson == null) {
            return missing("solo_lesson_missing");
        }

        if (!lesson.active) {
            return forbidden("solo_lesson_inactive");
        }

        if (!blank(lesson.madrassaId)) {
            return forbidden("solo_content_is_madrassa_owned");
        }

        value.id = value.id.trim();
        value.learnerId = value.learnerId.trim();
        value.lessonId = lessonId;
        value.madrassaId = null;
        value.updatedAt = System.currentTimeMillis();

        LearningProgress existing =
                progress.findByLearnerAndLesson(
                        value.learnerId,
                        lessonId
                );

        if (existing != null) {
            if (existing.madrassaId != null) {
                return forbidden("solo_progress_tenant_conflict");
            }

            existing.status = value.status;
            existing.updatedAt = value.updatedAt;

            return progress.update(existing)
                    ? OperationResult.success(existing)
                    : failed("solo_progress_update_failed");
        }

        return progress.save(value)
                ? OperationResult.success(value)
                : failed("solo_progress_save_failed");
    }

    private boolean authorizedScope(String madrassaId) {
        if (accessContext == null) {
            return false;
        }

        if (blank(madrassaId)) {
            return accessContext.allowsGlobal();
        }

        return accessContext.allowsMadrassa(madrassaId);
    }

    private boolean isEnrolled(
            Student student,
            String programmeId
    ) {
        if (student == null || blank(programmeId)) {
            return false;
        }

        if (student.programmeIds == null) {
            return false;
        }

        String target = programmeId.trim();

        for (String enrolledId : student.programmeIds) {
            if (!blank(enrolledId)
                    && target.equals(enrolledId.trim())) {
                return true;
            }
        }

        return false;
    }

    private static boolean blank(String value) {
        return value == null
                || value.trim().isEmpty();
    }

    private static <T> OperationResult<T> invalid(
            String code
    ) {
        return OperationResult.validationError(
                code,
                "Academic data is invalid."
        );
    }

    private static <T> OperationResult<T> missing(
            String code
    ) {
        return OperationResult.notFound(
                code,
                "Academic reference was not found."
        );
    }

    private static <T> OperationResult<T> forbidden(
            String code
    ) {
        return OperationResult.forbidden(
                code,
                "Academic relationship is not authorized."
        );
    }

    private static <T> OperationResult<T> conflict(
            String code
    ) {
        return OperationResult.conflict(
                code,
                "Academic record already exists."
        );
    }

    private static <T> OperationResult<T> failed(
            String code
    ) {
        return OperationResult.failed(
                code,
                "Academic record could not be saved."
        );
    }
}
