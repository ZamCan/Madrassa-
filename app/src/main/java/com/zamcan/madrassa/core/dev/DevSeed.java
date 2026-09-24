package com.zamcan.madrassa.core.dev;

import android.content.Context;

import com.zamcan.madrassa.data.local.EduNoorDatabase;
import com.zamcan.madrassa.data.model.AccountCredential;
import com.zamcan.madrassa.data.model.AccountRole;
import com.zamcan.madrassa.data.model.AcademicUnit;
import com.zamcan.madrassa.data.model.ApprovalStatus;
import com.zamcan.madrassa.data.model.ClassGroup;
import com.zamcan.madrassa.data.model.Course;
import com.zamcan.madrassa.data.model.Fee;
import com.zamcan.madrassa.data.model.FeeStatus;
import com.zamcan.madrassa.data.model.LearningProgress;
import com.zamcan.madrassa.data.model.Lesson;
import com.zamcan.madrassa.data.model.Madrassa;
import com.zamcan.madrassa.data.model.MadrassaPhone;
import com.zamcan.madrassa.data.model.Parent;
import com.zamcan.madrassa.data.model.ProgressStatus;
import com.zamcan.madrassa.data.model.Programme;
import com.zamcan.madrassa.data.model.Student;
import com.zamcan.madrassa.data.model.Ustadh;
import com.zamcan.madrassa.data.repository.AcademicUnitRepository;
import com.zamcan.madrassa.data.repository.ClassRepository;
import com.zamcan.madrassa.data.repository.CourseRepository;
import com.zamcan.madrassa.data.repository.CredentialRepository;
import com.zamcan.madrassa.data.repository.FeeRepository;
import com.zamcan.madrassa.data.repository.HeadUstadhProvisioningRepository;
import com.zamcan.madrassa.data.repository.LearningProgressRepository;
import com.zamcan.madrassa.data.repository.LessonRepository;
import com.zamcan.madrassa.data.repository.MadrassaPhoneRepository;
import com.zamcan.madrassa.data.repository.MadrassaRepository;
import com.zamcan.madrassa.data.repository.ParentProvisioningRepository;
import com.zamcan.madrassa.data.repository.ProgrammeRepository;
import com.zamcan.madrassa.data.repository.StudentRepository;
import com.zamcan.madrassa.domain.auth.Pbkdf2PasswordVerifier;

/**
 * =========================================================================
 * DEV-ONLY DEMO DATA.  Flip ENABLED to false before any production build.
 * =========================================================================
 *
 * Seeds one small, fully wired madrassa through the SAME provisioning
 * repositories the registration flow uses (no raw SQL, no shortcuts),
 * so the dashboards, lists and gateways can be reviewed on a second
 * device exactly as they will behave with real data.
 *
 * Sample credentials (also in DEV_CREDENTIALS.md at the repo root):
 *
 *   Ustadh  (Madrassa) : identifier "Madrassa Ya Kioo"
 *                        (or phone 0712 000 111)
 *                        password   Salama@2026
 *
 *   Parent             : phone 0713 000 222
 *                        password Amana2026
 *
 * Idempotent: seeding is skipped when the demo madrassa already
 * exists. A failure here never blocks boot - it is dev convenience,
 * not app state.
 */
public final class DevSeed {

    public static final boolean ENABLED = true;

    public static final String MADRASSA_ID = "MAD-DEV-001";
    public static final String MADRASSA_NAME = "Madrassa Ya Kioo";
    public static final String MADRASSA_PHONE = "+255712000111";

    public static final String USTADH_ID = "UST-DEV-001";
    public static final String USTADH_NAME = "Ustadh Jabir Mkuu";
    public static final String USTADH_PASSWORD = "Salama@2026";

    public static final String PARENT_ID = "PAR-DEV-001";
    public static final String PARENT_NAME = "Bi. Amina Mzazi";
    public static final String PARENT_PHONE = "+255713000222";
    public static final String PARENT_PASSWORD = "Amana2026";

    public static final String CLASS_ID = "CLS-DEV-001";
    public static final String STUDENT_ID = "STU-DEV-001";
    public static final String STUDENT_NAME = "Amina Binti Amana";

    private DevSeed() {
    }

    /** Runs the seed once per installation. Safe on any thread. */
    public static void seedIfEmpty(Context context) {

        if (!ENABLED || context == null) {
            return;
        }

        try {

            MadrassaRepository madrassas =
                    new MadrassaRepository(context);

            if (madrassas.existsById(MADRASSA_ID)) {
                return;
            }

            seed(context);

        } catch (Exception error) {
            // Dev convenience only - never block boot.
        }
    }

    private static void seed(Context context) {

        long now = System.currentTimeMillis();

        EduNoorDatabase database = new EduNoorDatabase(context);

        /*
         * 1. THE MADRASSA - approved and active.
         */
        Madrassa madrassa = new Madrassa();
        madrassa.id = MADRASSA_ID;
        madrassa.name = MADRASSA_NAME;
        madrassa.type = "QURAN";
        madrassa.administrationType = "COMMUNITY";
        madrassa.region = "Dar es Salaam";
        madrassa.district = "Ilala";
        madrassa.ward = "Upanga";
        madrassa.area = "Kisutu";
        madrassa.nearbyLandmark = "Msikitu wa Kisutu";
        madrassa.masjidName = "Msikitu wa Kisutu";
        madrassa.masjidLocation = "Kisutu, Ilala";
        madrassa.phone = MADRASSA_PHONE;
        madrassa.ustadhCount = 1;
        madrassa.approvalStatus = ApprovalStatus.ACTIVE;
        madrassa.submittedAt = now;
        madrassa.approvedAt = now;

        new MadrassaRepository(context).save(madrassa);

        /*
         * 2. THE REGISTERED PHONE - the ustadh identifier path.
         */
        MadrassaPhone phone = new MadrassaPhone();
        phone.id = "PHN-DEV-001";
        phone.madrassaId = MADRASSA_ID;
        phone.phone = MADRASSA_PHONE;
        phone.label = "Ofisi";
        phone.primary = true;
        phone.active = true;
        phone.createdAt = now;
        phone.verifiedAt = now;

        new MadrassaPhoneRepository(context).save(phone);

        /*
         * 3. THE HEAD USTADH + CREDENTIAL - via the real
         *    provisioning transaction.
         */
        Ustadh ustadh = new Ustadh();
        ustadh.id = USTADH_ID;
        ustadh.madrassaId = MADRASSA_ID;
        ustadh.fullName = USTADH_NAME;
        ustadh.phone = MADRASSA_PHONE;
        ustadh.email = "dev@madrassa.example";
        ustadh.headUstadh = true;
        ustadh.active = true;

        madrassa.headUstadhId = USTADH_ID;

        new HeadUstadhProvisioningRepository(context).provision(
                madrassa,
                ustadh,
                credential(
                        USTADH_ID,
                        AccountRole.USTADH,
                        MADRASSA_ID,
                        USTADH_PASSWORD,
                        now
                )
        );

        /*
         * 4. THE PARENT + CREDENTIAL - via the parent
         *    provisioning transaction.
         */
        Parent parent = new Parent();
        parent.id = PARENT_ID;
        parent.madrassaId = MADRASSA_ID;
        parent.fullName = PARENT_NAME;
        parent.phone = PARENT_PHONE;
        parent.firstLogin = false;
        parent.active = true;

        new ParentProvisioningRepository(context).provision(
                parent,
                credential(
                        PARENT_ID,
                        AccountRole.PARENT,
                        MADRASSA_ID,
                        PARENT_PASSWORD,
                        now
                )
        );

        /*
         * 5. THE ACADEMIC SPINE - programme, course, unit,
         *    lessons (schema-complete chain).
         */
        Programme programme = new Programme();
        programme.id = "PRG-DEV-001";
        programme.madrassaId = MADRASSA_ID;
        programme.name = "Qurani Tukufu";
        programme.category = "QURAN";
        programme.defaultProgramme = true;
        programme.active = true;

        new ProgrammeRepository(database).save(programme);

        Course course = new Course();
        course.id = "CRS-DEV-001";
        course.programmeId = programme.id;
        course.madrassaId = MADRASSA_ID;
        course.name = "Kitabu cha Darasa la Kwanza";
        course.levelNumber = 1;
        course.active = true;

        new CourseRepository(database).save(course);

        AcademicUnit unit = new AcademicUnit();
        unit.id = "UNT-DEV-001";
        unit.courseId = course.id;
        unit.madrassaId = MADRASSA_ID;
        unit.name = "Wiki ya 1";
        unit.orderIndex = 1;

        new AcademicUnitRepository(database).save(unit);

        Lesson lessonSwala = new Lesson();
        lessonSwala.id = "LSN-DEV-001";
        lessonSwala.unitId = unit.id;
        lessonSwala.madrassaId = MADRASSA_ID;
        lessonSwala.name = "Swala: Wudhu na Nyusuri";
        lessonSwala.contentType = "TEXT";
        lessonSwala.orderIndex = 1;
        lessonSwala.active = true;

        Lesson lessonQuran = new Lesson();
        lessonQuran.id = "LSN-DEV-002";
        lessonQuran.unitId = unit.id;
        lessonQuran.madrassaId = MADRASSA_ID;
        lessonQuran.name = "Al-Fatihah: Kusoma na Kuhifadhi";
        lessonQuran.contentType = "TEXT";
        lessonQuran.orderIndex = 2;
        lessonQuran.active = true;

        LessonRepository lessons = new LessonRepository(database);
        lessons.save(lessonSwala);
        lessons.save(lessonQuran);

        /*
         * 6. THE CLASS - taught by the head ustadh.
         */
        ClassGroup classGroup = new ClassGroup();
        classGroup.id = CLASS_ID;
        classGroup.madrassaId = MADRASSA_ID;
        classGroup.code = "D1";
        classGroup.name = "Darasa la Kwanza";
        classGroup.teacherId = USTADH_ID;
        classGroup.orderIndex = 1;
        classGroup.active = true;

        new ClassRepository(database).save(classGroup);

        /*
         * 7. THE STUDENT - child of the demo parent.
         */
        Student student = new Student();
        student.id = STUDENT_ID;
        student.madrassaId = MADRASSA_ID;
        student.parentId = PARENT_ID;
        student.fullName = STUDENT_NAME;
        student.parentGuardianName = PARENT_NAME;
        student.mainPhone = PARENT_PHONE;
        student.emergencyPhone = MADRASSA_PHONE;
        student.classId = CLASS_ID;
        student.quranLevel = "4";
        student.hifzLevel = "2";
        student.programmeIds.add(programme.id);
        student.active = true;

        new StudentRepository(database).save(student);

        /*
         * 8. FEES - one outstanding (madeni), one settled.
         */
        FeeRepository fees = new FeeRepository(database);

        Fee due = new Fee();
        due.id = "FEE-DEV-001";
        due.studentId = STUDENT_ID;
        due.madrassaId = MADRASSA_ID;
        due.type = "Ada ya Mwezi";
        due.amount = 15000;
        due.deadline = "2026-10-05";
        due.status = FeeStatus.UNPAID;
        due.submittedAt = now;
        due.confirmedAt = 0;
        due.lockedAt = 0;

        Fee paid = new Fee();
        paid.id = "FEE-DEV-002";
        paid.studentId = STUDENT_ID;
        paid.madrassaId = MADRASSA_ID;
        paid.type = "Ada ya Usajili";
        paid.amount = 20000;
        paid.deadline = "2026-09-01";
        paid.status = FeeStatus.PAID;
        paid.submittedAt = now - 86400000L * 20;
        paid.confirmedAt = now - 86400000L * 19;
        paid.lockedAt = now - 86400000L * 19;
        paid.submittedBy = USTADH_ID;
        paid.confirmedBy = USTADH_ID;

        fees.save(due);
        fees.save(paid);

        /*
         * 9. LEARNING PROGRESS - so the progress section
         *    shows real rows with statuses.
         */
        LearningProgressRepository progress =
                new LearningProgressRepository(database);

        progress.save(progress(
                "PRG-DEV-L1",
                STUDENT_ID,
                lessonSwala.id,
                ProgressStatus.COMPLETED,
                now - 86400000L * 2
        ));

        progress.save(progress(
                "PRG-DEV-L2",
                STUDENT_ID,
                lessonQuran.id,
                ProgressStatus.IN_PROGRESS,
                now
        ));
    }

    private static AccountCredential credential(
            String accountId,
            AccountRole role,
            String madrassaId,
            String rawPassword,
            long now
    ) {
        Pbkdf2PasswordVerifier.PasswordHashResult hashed =
                new Pbkdf2PasswordVerifier().hash(rawPassword);

        AccountCredential credential = new AccountCredential();
        credential.id = "CRD-" + accountId;
        credential.accountId = accountId;
        credential.role = role;
        credential.madrassaId = madrassaId;
        credential.passwordHash = hashed.hash;
        credential.passwordSalt = hashed.salt;
        credential.active = true;
        credential.firstLogin = false;
        credential.createdAt = now;
        credential.updatedAt = now;
        credential.lastLoginAt = 0;

        return credential;
    }

    private static LearningProgress progress(
            String id,
            String learnerId,
            String lessonId,
            ProgressStatus status,
            long updatedAt
    ) {
        LearningProgress entry = new LearningProgress();
        entry.id = id;
        entry.learnerId = learnerId;
        entry.lessonId = lessonId;
        entry.madrassaId = MADRASSA_ID;
        entry.status = status;
        entry.updatedAt = updatedAt;

        return entry;
    }
}
