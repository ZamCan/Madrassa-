import com.zamcan.madrassa.core.validation.EduNoorRules;
import com.zamcan.madrassa.data.model.AcademicUnit;
import com.zamcan.madrassa.data.model.AccountCredential;
import com.zamcan.madrassa.data.model.AccountRole;
import com.zamcan.madrassa.data.model.ApprovalStatus;
import com.zamcan.madrassa.data.model.Assessment;
import com.zamcan.madrassa.data.model.Assignment;
import com.zamcan.madrassa.data.model.ClassGroup;
import com.zamcan.madrassa.data.model.Course;
import com.zamcan.madrassa.data.model.LearningProgress;
import com.zamcan.madrassa.data.model.Lesson;
import com.zamcan.madrassa.data.model.Parent;
import com.zamcan.madrassa.data.model.Programme;
import com.zamcan.madrassa.data.model.ProgressStatus;
import com.zamcan.madrassa.data.model.Student;
import com.zamcan.madrassa.data.model.Ustadh;
import com.zamcan.madrassa.domain.academic.AcademicCoreService;
import com.zamcan.madrassa.domain.authorization.AcademicAccessContext;
import com.zamcan.madrassa.domain.authorization.MadrassaAccessContext;
import com.zamcan.madrassa.domain.authorization.UstadhAccessPolicy;
import com.zamcan.madrassa.domain.authorization.parent.ParentAccessPolicy;
import com.zamcan.madrassa.domain.auth.CredentialPolicy;
import com.zamcan.madrassa.domain.auth.PasswordVerifier;
import com.zamcan.madrassa.domain.auth.parent.ParentAuthenticationResult;
import com.zamcan.madrassa.domain.auth.parent.ParentAuthenticationService;
import com.zamcan.madrassa.domain.authorization.parent.ParentIdentityResolver;
import com.zamcan.madrassa.domain.common.OperationResult;
import com.zamcan.madrassa.domain.common.TenantPolicy;
import com.zamcan.madrassa.domain.geography.LocationCatalog;
import com.zamcan.madrassa.domain.programme.ProgrammeSetupService;
import com.zamcan.madrassa.domain.repository.AcademicUnitStore;
import com.zamcan.madrassa.domain.repository.AssessmentStore;
import com.zamcan.madrassa.domain.repository.AssignmentStore;
import com.zamcan.madrassa.domain.repository.ClassStore;
import com.zamcan.madrassa.domain.repository.CourseStore;
import com.zamcan.madrassa.domain.repository.CredentialStore;
import com.zamcan.madrassa.domain.repository.LearningMaterialStore;
import com.zamcan.madrassa.domain.repository.LearningProgressStore;
import com.zamcan.madrassa.domain.repository.LessonStore;
import com.zamcan.madrassa.domain.repository.ParentStore;
import com.zamcan.madrassa.domain.repository.ProgrammeStore;
import com.zamcan.madrassa.domain.repository.StudentStore;
import com.zamcan.madrassa.domain.students.StudentClassAssignmentService;
import com.zamcan.madrassa.domain.students.StudentProgrammeEnrollmentService;
import com.zamcan.madrassa.domain.salah.AdhanTriggerPolicy;
import com.zamcan.madrassa.domain.salah.PrayerTime;
import com.zamcan.madrassa.domain.salah.PrayerTimesCalculator;
import com.zamcan.madrassa.domain.salah.QiblaCalculator;
import com.zamcan.madrassa.domain.salah.SalahReminderEngine;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Desktop JVM security and relationship checks.
 *
 * Run from the repository root:
 *   javac -encoding UTF-8 -d /tmp/madrassa-security-check \
 *       -sourcepath app/src/main/java manual/security-check/SecurityCheck.java
 *   java -cp /tmp/madrassa-security-check SecurityCheck
 */
public final class SecurityCheck {

    private static int passed;
    private static final List<String> failures = new ArrayList<>();

    public static void main(String[] args) {
        tenantChecks();
        parentChecks();
        academicChecks();
        ustadhChecks();
        programmeChecks();
        prayerChecks();
        validationChecks();

        System.out.println();
        System.out.println(passed + " passed, " + failures.size() + " failed");
        if (!failures.isEmpty()) {
            for (String failure : failures) {
                System.out.println("  FAILED: " + failure);
            }
            System.exit(1);
        }
    }

    private static void tenantChecks() {
        check("same tenant", TenantPolicy.sameMadrassa("m-a", " m-a "));
        check("different tenant rejected", !TenantPolicy.sameMadrassa("m-a", "m-b"));
        check("blank tenant rejected", !TenantPolicy.sameMadrassa(" ", " "));
        expectSecurity("cross tenant require", () ->
                TenantPolicy.requireSameMadrassa("m-a", "m-b"));
    }

    private static void parentChecks() {
        Parent parent = new Parent();
        parent.id = "p-a";
        parent.madrassaId = "m-a";
        parent.active = true;
        parent.phone = "+255700000001";

        Student own = student("s-a", "m-a", "p-a");
        Student foreign = student("s-b", "m-b", "p-b");

        check("parent owns own child", ParentAccessPolicy.canAccessStudent(parent, own));
        check("cross tenant child rejected", !ParentAccessPolicy.canAccessStudent(parent, foreign));
        check("unlinked same tenant rejected", !ParentAccessPolicy.canAccessStudent(
                parent, student("s-c", "m-a", "p-other")));

        ParentAuthenticationService auth = new ParentAuthenticationService(
                new ParentIdentityResolver() {
                    @Override
                    public Parent findByPhone(String phone) {
                        return "+255700000001".equals(phone) ? parent : null;
                    }

                    @Override
                    public Parent resolve(String identifier) {
                        return findByPhone(identifier);
                    }
                },
                credentialStore(credential("cred-a", "p-a", "m-a")),
                parentStore(parent),
                new AlwaysMatchVerifier()
        );

        ParentAuthenticationResult ownResult = auth.authenticate(
                "+255700000001",
                "Password1"
        );
        check("parent auth accepts matching tenant",
                ownResult.status == ParentAuthenticationResult.Status.SUCCESS);

        ParentAuthenticationService crossAuth = new ParentAuthenticationService(
                new ParentIdentityResolver() {
                    @Override
                    public Parent findByPhone(String phone) {
                        return parent;
                    }

                    @Override
                    public Parent resolve(String identifier) {
                        return parent;
                    }
                },
                credentialStore(credential("cred-b", "p-a", "m-b")),
                parentStore(parent),
                new AlwaysMatchVerifier()
        );
        check("parent auth rejects cross tenant credential",
                crossAuth.authenticate("+255700000001", "Password1").status
                        == ParentAuthenticationResult.Status.INVALID_CREDENTIALS);

        check("credential tenant policy trims",
                CredentialPolicy.belongsToMadrassa(
                        credential("c", "a", " m-a "), "m-a"));
    }

    private static void academicChecks() {
        Fixture fixture = new Fixture();

        Course foreignCourse = course("c-b", "m-b", "p-b");
        Course attempted = course("c-x", "m-a", "p-b");
        OperationResult<Course> courseResult = fixture.core()
                .createCourse(attempted);
        checkCode("cross tenant course rejected", courseResult,
                "course_tenant_mismatch");

        Student student = fixture.student;
        Assignment foreignAssignment = assignment("a-b", "m-b", "p-b");
        fixture.assignmentAll.add(foreignAssignment);
        fixture.assignmentValues.put(
                foreignAssignment.id,
                foreignAssignment
        );
        Assessment crossAssessment = new Assessment();
        crossAssessment.id = "as-x";
        crossAssessment.madrassaId = "m-a";
        crossAssessment.studentId = student.id;
        crossAssessment.assignmentId = foreignAssignment.id;
        crossAssessment.status = ProgressStatus.IN_PROGRESS;
        crossAssessment.score = 1;
        checkCode("cross tenant assessment rejected",
                fixture.core().recordAssessment(crossAssessment),
                "assessment_assignment_tenant_mismatch");

        LearningProgress foreignProgress = new LearningProgress();
        foreignProgress.id = "pr-x";
        foreignProgress.madrassaId = "m-a";
        foreignProgress.learnerId = student.id;
        foreignProgress.lessonId = "l-b";
        foreignProgress.status = ProgressStatus.IN_PROGRESS;
        checkCode("cross tenant progress rejected",
                fixture.core().recordStudentProgress(foreignProgress),
                "progress_lesson_tenant_mismatch");

        StudentClassAssignmentService assignment =
                new StudentClassAssignmentService(
                        fixture.studentStore,
                        fixture.classStore,
                        MadrassaAccessContext.forMadrassa("m-a")
                );
        checkCode("cross tenant class rejected",
                assignment.assign(student.id, "class-b"),
                "class_assignment_tenant_mismatch");

        StudentProgrammeEnrollmentService enrollment =
                new StudentProgrammeEnrollmentService(
                        fixture.studentStore,
                        fixture.programmeStore
                );
        expectSecurity("cross tenant programme enrollment", () ->
                enrollment.enroll(student.id, "p-b"));
    }

    private static void ustadhChecks() {
        Ustadh ustadh = new Ustadh();
        ustadh.id = "u-a";
        ustadh.madrassaId = "m-a";
        ustadh.active = true;
        check("ustadh own tenant allowed",
                UstadhAccessPolicy.belongsToMadrassa(ustadh, " m-a "));
        check("ustadh foreign tenant rejected",
                !UstadhAccessPolicy.belongsToMadrassa(ustadh, "m-b"));
        ustadh.active = false;
        check("inactive ustadh cannot login",
                !UstadhAccessPolicy.canLogin(ustadh, ApprovalStatus.ACTIVE));
    }

    private static void programmeChecks() {
        Map<String, Object> values = new HashMap<>();
        List<Object> all = new ArrayList<>();
        ProgrammeStore store = store(ProgrammeStore.class, values, all);
        ProgrammeSetupService setup = new ProgrammeSetupService(store);
        List<Programme> first = setup.ensureMadrassaDefaults("m-a");
        List<Programme> second = setup.ensureMadrassaDefaults("m-a");
        check("default programmes created", first.size() == 3);
        check("default setup idempotent", second.size() == 3 && all.size() == 3);
    }

    private static void prayerChecks() {
        List<PrayerTime> times = PrayerTimesCalculator.calculate(
                LocalDate.of(2026, 9, 24),
                LocationCatalog.darEsSalaam()
        );
        check("six prayer events calculated", times.size() == 6);
        check("sunrise is present for timetable",
                times.get(1).prayer == PrayerTime.Prayer.SUNRISE);
        check("asr precedes maghrib",
                times.get(3).time.isBefore(times.get(4).time));
        check("sunrise is not due", AdhanTriggerPolicy.duePrayer(
                LocalDateTime.of(
                        LocalDate.of(2026, 9, 24),
                        times.get(1).time
                ),
                times,
                null
        ) != PrayerTime.Prayer.SUNRISE);
        check("fajr is due at exact minute", AdhanTriggerPolicy.duePrayer(
                LocalDateTime.of(
                        LocalDate.of(2026, 9, 24),
                        times.get(0).time
                ),
                times,
                null
        ) == PrayerTime.Prayer.FAJR);
        check("early reminder rejected", !SalahReminderEngine.mayPlayAdhan(
                LocalDateTime.of(2026, 9, 24, 0, 0),
                times.get(0)
        ));
        double bearing = QiblaCalculator.bearingDegrees(-6.7924, 39.2083);
        check("qibla bearing is finite", bearing >= 0 && bearing < 360);
    }

    private static void validationChecks() {
        check("ustadh password policy", !EduNoorRules.validUstadhPassword("weak"));
        check("strong ustadh password policy", EduNoorRules.validUstadhPassword("Madrassa1!"));
        check("parent password policy", EduNoorRules.validParentPassword("Parent1"));
    }

    private static Student student(String id, String tenant, String parentId) {
        Student value = new Student();
        value.id = id;
        value.madrassaId = tenant;
        value.parentId = parentId;
        value.active = true;
        value.programmeIds = new ArrayList<>();
        value.fullName = id;
        value.parentGuardianName = "Guardian";
        value.mainPhone = "+255700000000";
        value.classId = "class-" + tenant;
        value.quranLevel = "1";
        value.hifzLevel = "1";
        return value;
    }

    private static Course course(String id, String tenant, String programmeId) {
        Course value = new Course();
        value.id = id;
        value.madrassaId = tenant;
        value.programmeId = programmeId;
        value.name = id;
        value.levelNumber = 1;
        value.active = true;
        return value;
    }

    private static Assignment assignment(String id, String tenant, String programmeId) {
        Assignment value = new Assignment();
        value.id = id;
        value.madrassaId = tenant;
        value.programmeId = programmeId;
        value.title = id;
        value.maxPoints = 10;
        value.dueDate = 1;
        value.active = true;
        return value;
    }

    private static AccountCredential credential(
            String id,
            String accountId,
            String tenant
    ) {
        AccountCredential value = new AccountCredential();
        value.id = id;
        value.accountId = accountId;
        value.role = AccountRole.PARENT;
        value.madrassaId = tenant;
        value.passwordHash = "hash";
        value.passwordSalt = "salt";
        value.active = true;
        value.firstLogin = false;
        return value;
    }

    private static ParentStore parentStore(Parent parent) {
        Map<String, Object> values = new HashMap<>();
        List<Object> all = new ArrayList<>();
        values.put(parent.id, parent);
        all.add(parent);
        return store(ParentStore.class, values, all);
    }

    private static CredentialStore credentialStore(AccountCredential credential) {
        Map<String, Object> values = new HashMap<>();
        values.put(credential.accountId, credential);
        return store(CredentialStore.class, values, new ArrayList<>());
    }

    @SuppressWarnings("unchecked")
    private static <T> T store(
            Class<T> type,
            Map<String, Object> byId,
            List<Object> all
    ) {
        InvocationHandler handler = (proxy, method, args) -> {
            String name = method.getName();
            if (name.equals("findById")) {
                return byId.get(String.valueOf(args[0]));
            }
            if (name.equals("findByAccount")) {
                return byId.get(String.valueOf(args[0]));
            }
            if (name.equals("findByPhone")) {
                for (Object value : all) {
                    if (value instanceof Parent
                            && args[0].equals(((Parent) value).phone)) {
                        return value;
                    }
                }
                return null;
            }
            if (name.equals("findByLearnerAndLesson")) {
                for (Object value : all) {
                    LearningProgress progress = (LearningProgress) value;
                    if (progress.learnerId.equals(args[0])
                            && progress.lessonId.equals(args[1])) {
                        return progress;
                    }
                }
                return null;
            }
            if (name.startsWith("findBy")) {
                String field = name.substring("findBy".length());
                List<Object> result = new ArrayList<>();
                for (Object value : all) {
                    if (matches(value, field, args == null || args.length == 0
                            ? null : args[0])) {
                        result.add(value);
                    }
                }
                return result;
            }
            if (name.equals("save")) {
                Object value = args[0];
                all.add(value);
                String id = field(value, "id");
                if (id != null) {
                    byId.put(id, value);
                }
                return true;
            }
            if (name.equals("update")) {
                Object value = args[0];
                all.removeIf(existing -> sameId(existing, value));
                all.add(value);
                String id = field(value, "id");
                if (id != null) {
                    byId.put(id, value);
                }
                return true;
            }
            if (name.equals("existsByPhone")) {
                return false;
            }
            if (method.getReturnType() == boolean.class) {
                return false;
            }
            if (method.getReturnType() == int.class) {
                return 0;
            }
            if (List.class.isAssignableFrom(method.getReturnType())) {
                return new ArrayList<>();
            }
            return null;
        };

        return (T) Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class<?>[]{type},
                handler
        );
    }

    private static boolean matches(Object value, String field, Object wanted) {
        if (wanted == null) {
            return true;
        }
        Object actual = field(value, normalizeField(field));
        return actual != null && wanted.toString().equals(actual.toString());
    }

    private static String normalizeField(String field) {
        if (field.equals("Madrassa")) return "madrassaId";
        if (field.equals("Programme")) return "programmeId";
        if (field.equals("Student")) return "studentId";
        if (field.equals("Learner")) return "learnerId";
        if (field.equals("Lesson")) return "lessonId";
        if (field.equals("Assignment")) return "assignmentId";
        return Character.toLowerCase(field.charAt(0)) + field.substring(1);
    }

    private static boolean sameId(Object first, Object second) {
        return field(first, "id") != null
                && field(first, "id").equals(field(second, "id"));
    }

    private static String field(Object value, String name) {
        try {
            return String.valueOf(value.getClass().getField(name).get(value));
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static void check(String name, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("  PASS  " + name);
        } else {
            failures.add(name);
            System.out.println("  FAIL  " + name);
        }
    }

    private static void checkCode(
            String name,
            OperationResult<?> result,
            String code
    ) {
        check(name + " [" + (result == null ? "null" : result.getCode()) + "]",
                result != null && code.equals(result.getCode()));
    }

    private static void expectSecurity(String name, Runnable action) {
        try {
            action.run();
            check(name, false);
        } catch (SecurityException expected) {
            check(name, true);
        } catch (RuntimeException other) {
            check(name + " [" + other.getClass().getSimpleName() + "]", false);
        }
    }

    private static final class AlwaysMatchVerifier implements PasswordVerifier {
        @Override
        public boolean matches(String rawPassword, String hash, String salt) {
            return true;
        }

        @Override
        public PasswordHashResult hash(String rawPassword) {
            return new PasswordHashResult("hash", "salt");
        }
    }

    private static final class Fixture {
        final Map<String, Object> programmeValues = new HashMap<>();
        final List<Object> programmeAll = new ArrayList<>();
        final ProgrammeStore programmeStore = store(
                ProgrammeStore.class, programmeValues, programmeAll
        );
        final Map<String, Object> courseValues = new HashMap<>();
        final List<Object> courseAll = new ArrayList<>();
        final CourseStore courseStore = store(CourseStore.class, courseValues, courseAll);
        final Map<String, Object> unitValues = new HashMap<>();
        final List<Object> unitAll = new ArrayList<>();
        final AcademicUnitStore unitStore = store(AcademicUnitStore.class, unitValues, unitAll);
        final Map<String, Object> lessonValues = new HashMap<>();
        final List<Object> lessonAll = new ArrayList<>();
        final LessonStore lessonStore = store(LessonStore.class, lessonValues, lessonAll);
        final Map<String, Object> materialValues = new HashMap<>();
        final List<Object> materialAll = new ArrayList<>();
        final LearningMaterialStore materialStore = store(
                LearningMaterialStore.class, materialValues, materialAll
        );
        final Map<String, Object> assignmentValues = new HashMap<>();
        final List<Object> assignmentAll = new ArrayList<>();
        final AssignmentStore assignmentStore = store(
                AssignmentStore.class, assignmentValues, assignmentAll
        );
        final Map<String, Object> assessmentValues = new HashMap<>();
        final List<Object> assessmentAll = new ArrayList<>();
        final AssessmentStore assessmentStore = store(
                AssessmentStore.class, assessmentValues, assessmentAll
        );
        final Map<String, Object> progressValues = new HashMap<>();
        final List<Object> progressAll = new ArrayList<>();
        final LearningProgressStore progressStore = store(
                LearningProgressStore.class, progressValues, progressAll
        );
        final Map<String, Object> studentValues = new HashMap<>();
        final List<Object> studentAll = new ArrayList<>();
        final StudentStore studentStore = store(StudentStore.class, studentValues, studentAll);
        final Map<String, Object> classValues = new HashMap<>();
        final List<Object> classAll = new ArrayList<>();
        final ClassStore classStore = store(ClassStore.class, classValues, classAll);

        final Programme programmeA = programme("p-a", "m-a");
        final Programme programmeB = programme("p-b", "m-b");
        final Student student = student("s-a", "m-a", "p-a");
        final ClassGroup classA = classGroup("class-a", "m-a");
        final ClassGroup classB = classGroup("class-b", "m-b");
        final Lesson lessonA = lesson("l-a", "m-a", "unit-a");
        final Lesson lessonB = lesson("l-b", "m-b", "unit-b");
        final AcademicUnit unitA = unit("unit-a", "m-a", "c-a");
        final AcademicUnit unitB = unit("unit-b", "m-b", "c-b");
        final Course courseA = course("c-a", "m-a", "p-a");
        final Course courseB = course("c-b", "m-b", "p-b");
        final Assignment assignmentA = assignment("a-a", "m-a", "p-a");

        Fixture() {
            programmeAll.add(programmeA);
            programmeAll.add(programmeB);
            programmeValues.put(programmeA.id, programmeA);
            programmeValues.put(programmeB.id, programmeB);
            student.programmeIds.add("p-a");
            studentAll.add(student);
            studentValues.put(student.id, student);
            classAll.add(classA);
            classAll.add(classB);
            classValues.put(classA.id, classA);
            classValues.put(classB.id, classB);
            courseAll.add(courseA);
            courseAll.add(courseB);
            courseValues.put(courseA.id, courseA);
            courseValues.put(courseB.id, courseB);
            unitAll.add(unitA);
            unitAll.add(unitB);
            unitValues.put(unitA.id, unitA);
            unitValues.put(unitB.id, unitB);
            lessonAll.add(lessonA);
            lessonAll.add(lessonB);
            lessonValues.put(lessonA.id, lessonA);
            lessonValues.put(lessonB.id, lessonB);
            assignmentAll.add(assignmentA);
            assignmentValues.put(assignmentA.id, assignmentA);
        }

        AcademicCoreService core() {
            return new AcademicCoreService(
                    programmeStore,
                    courseStore,
                    unitStore,
                    lessonStore,
                    materialStore,
                    assignmentStore,
                    assessmentStore,
                    progressStore,
                    studentStore,
                    AcademicAccessContext.forMadrassa("m-a")
            );
        }

        private static Programme programme(String id, String tenant) {
            Programme value = new Programme();
            value.id = id;
            value.madrassaId = tenant;
            value.name = id;
            value.active = true;
            return value;
        }

        private static ClassGroup classGroup(String id, String tenant) {
            ClassGroup value = new ClassGroup();
            value.id = id;
            value.madrassaId = tenant;
            value.name = id;
            value.active = true;
            return value;
        }

        private static Lesson lesson(String id, String tenant, String unitId) {
            Lesson value = new Lesson();
            value.id = id;
            value.madrassaId = tenant;
            value.unitId = unitId;
            value.name = id;
            value.contentType = "TEXT";
            value.active = true;
            return value;
        }

        private static AcademicUnit unit(String id, String tenant, String courseId) {
            AcademicUnit value = new AcademicUnit();
            value.id = id;
            value.madrassaId = tenant;
            value.courseId = courseId;
            value.name = id;
            return value;
        }
    }
}
