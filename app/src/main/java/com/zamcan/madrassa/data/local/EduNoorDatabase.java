package com.zamcan.madrassa.data.local;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public final class EduNoorDatabase extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "edunoor.db";
    private static final int DATABASE_VERSION = 10;

    public EduNoorDatabase(Context context) {
        super(
                context.getApplicationContext(),
                DATABASE_NAME,
                null,
                DATABASE_VERSION
        );
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(
                "CREATE TABLE IF NOT EXISTS madrassas (" +
                        "id TEXT PRIMARY KEY," +
                        "name TEXT NOT NULL," +
                        "type TEXT NOT NULL," +
                        "administration_type TEXT NOT NULL," +
                        "region TEXT NOT NULL," +
                        "district TEXT NOT NULL," +
                        "ward TEXT NOT NULL," +
                        "area TEXT NOT NULL," +
                        "nearby_landmark TEXT," +
                        "masjid_name TEXT," +
                        "masjid_location TEXT," +
                        "head_ustadh_id TEXT," +
                        "phone TEXT NOT NULL," +
                        "secondary_phone TEXT," +
                        "email TEXT NOT NULL," +
                        "ustadh_count INTEGER NOT NULL," +
                        "approval_status TEXT," +
                        "rejection_reason TEXT," +
                        "submitted_at INTEGER NOT NULL," +
                        "approved_at INTEGER NOT NULL" +
                        ")"
        );

        db.execSQL(
                "CREATE TABLE IF NOT EXISTS madrassa_phones (" +
                        "id TEXT PRIMARY KEY," +
                        "madrassa_id TEXT NOT NULL," +
                        "phone TEXT NOT NULL," +
                        "label TEXT," +
                        "primary_phone INTEGER NOT NULL," +
                        "active INTEGER NOT NULL," +
                        "created_at INTEGER NOT NULL," +
                        "verified_at INTEGER NOT NULL" +
                        ")"
        );

        db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS idx_madrassa_phone_unique " +
                        "ON madrassa_phones(phone)"
        );

        db.execSQL(
                "CREATE INDEX IF NOT EXISTS idx_madrassa_phones_madrassa " +
                        "ON madrassa_phones(madrassa_id)"
        );

        db.execSQL(
                "CREATE TABLE IF NOT EXISTS credentials (" +
                        "id TEXT PRIMARY KEY," +
                        "account_id TEXT NOT NULL," +
                        "role TEXT NOT NULL," +
                        "madrassa_id TEXT NOT NULL," +
                        "password_hash TEXT NOT NULL," +
                        "password_salt TEXT NOT NULL," +
                        "active INTEGER NOT NULL," +
                        "first_login INTEGER NOT NULL," +
                        "created_at INTEGER NOT NULL," +
                        "updated_at INTEGER NOT NULL," +
                        "last_login_at INTEGER NOT NULL" +
                        ")"
        );

        db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS idx_credentials_account_role " +
                        "ON credentials(account_id, role)"
        );

        db.execSQL(
                "CREATE INDEX IF NOT EXISTS idx_credentials_madrassa " +
                        "ON credentials(madrassa_id)"
        );

        db.execSQL(
                "CREATE TABLE IF NOT EXISTS pending_registration_credentials (" +
                        "id TEXT PRIMARY KEY," +
                        "madrassa_id TEXT NOT NULL," +
                        "role TEXT NOT NULL," +
                        "password_hash TEXT NOT NULL," +
                        "password_salt TEXT NOT NULL," +
                        "created_at INTEGER NOT NULL," +
                        "updated_at INTEGER NOT NULL" +
                        ")"
        );

        db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS idx_pending_registration_madrassa_role " +
                        "ON pending_registration_credentials(madrassa_id, role)"
        );

        db.execSQL(
                "CREATE TABLE IF NOT EXISTS ustadhs (" +
                        "id TEXT PRIMARY KEY," +
                        "madrassa_id TEXT NOT NULL," +
                        "full_name TEXT NOT NULL," +
                        "phone TEXT NOT NULL," +
                        "email TEXT," +
                        "head_ustadh INTEGER NOT NULL," +
                        "active INTEGER NOT NULL" +
                        ")"
        );

        db.execSQL(
                "CREATE INDEX IF NOT EXISTS idx_ustadhs_madrassa " +
                        "ON ustadhs(madrassa_id)"
        );

        db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS idx_ustadhs_phone " +
                        "ON ustadhs(phone)"
        );

        db.execSQL(
                "CREATE TABLE IF NOT EXISTS parents (" +
                        "id TEXT PRIMARY KEY," +
                        "madrassa_id TEXT," +
                        "full_name TEXT NOT NULL," +
                        "phone TEXT NOT NULL," +
                        "first_login INTEGER NOT NULL," +
                        "active INTEGER NOT NULL" +
                        ")"
        );

        db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS idx_parents_phone " +
                        "ON parents(phone)"
        );

        db.execSQL(
                "CREATE INDEX IF NOT EXISTS idx_parents_madrassa " +
                        "ON parents(madrassa_id)"
        );

        db.execSQL(
                "CREATE TABLE IF NOT EXISTS students (" +
                        "id TEXT PRIMARY KEY," +
                        "madrassa_id TEXT NOT NULL," +
                        "parent_id TEXT," +
                        "full_name TEXT NOT NULL," +
                        "parent_guardian_name TEXT NOT NULL," +
                        "main_phone TEXT NOT NULL," +
                        "emergency_phone TEXT," +
                        "class_id TEXT NOT NULL," +
                        "quran_level TEXT NOT NULL," +
                        "hifz_level TEXT NOT NULL," +
                        "active INTEGER NOT NULL" +
                        ")"
        );

        db.execSQL(
                "CREATE INDEX IF NOT EXISTS idx_students_madrassa " +
                        "ON students(madrassa_id)"
        );

        db.execSQL(
                "CREATE INDEX IF NOT EXISTS idx_students_parent " +
                        "ON students(parent_id)"
        );

        db.execSQL(
                "CREATE TABLE IF NOT EXISTS parent_student_links (" +
                        "parent_id TEXT NOT NULL," +
                        "student_id TEXT NOT NULL," +
                        "created_at INTEGER NOT NULL," +
                        "PRIMARY KEY(parent_id, student_id)" +
                        ")"
        );

        db.execSQL(
                "CREATE INDEX IF NOT EXISTS idx_parent_student_parent " +
                        "ON parent_student_links(parent_id)"
        );

        db.execSQL(
                "CREATE INDEX IF NOT EXISTS idx_parent_student_student " +
                        "ON parent_student_links(student_id)"
        );

        db.execSQL(
                "CREATE TABLE IF NOT EXISTS student_programmes (" +
                        "student_id TEXT NOT NULL," +
                        "programme_id TEXT NOT NULL," +
                        "created_at INTEGER NOT NULL," +
                        "PRIMARY KEY(student_id, programme_id)" +
                        ")"
        );

        db.execSQL(
                "CREATE INDEX IF NOT EXISTS idx_student_programmes_student " +
                        "ON student_programmes(student_id)"
        );

        db.execSQL(
                "CREATE INDEX IF NOT EXISTS idx_student_programmes_programme " +
                        "ON student_programmes(programme_id)"
        );

        db.execSQL(
                "CREATE TABLE IF NOT EXISTS class_groups (" +
                        "id TEXT PRIMARY KEY," +
                        "madrassa_id TEXT NOT NULL," +
                        "code TEXT," +
                        "name TEXT NOT NULL," +
                        "teacher_id TEXT," +
                        "order_index INTEGER NOT NULL DEFAULT 0," +
                        "active INTEGER NOT NULL" +
                        ")"
        );

        db.execSQL(
                "CREATE INDEX IF NOT EXISTS idx_classes_madrassa " +
                        "ON class_groups(madrassa_id)"
        );

        db.execSQL(
                "CREATE TABLE IF NOT EXISTS programmes (" +
                        "id TEXT PRIMARY KEY," +
                        "madrassa_id TEXT," +
                        "name TEXT NOT NULL," +
                        "category TEXT," +
                        "default_programme INTEGER NOT NULL," +
                        "active INTEGER NOT NULL" +
                        ")"
        );

        db.execSQL(
                "CREATE INDEX IF NOT EXISTS idx_programmes_madrassa " +
                        "ON programmes(madrassa_id)"
        );

        db.execSQL(
                "CREATE TABLE IF NOT EXISTS attendance (" +
                        "id TEXT PRIMARY KEY," +
                        "student_id TEXT NOT NULL," +
                        "class_id TEXT NOT NULL," +
                        "date TEXT NOT NULL," +
                        "status TEXT NOT NULL," +
                        "note TEXT," +
                        "recorded_by TEXT" +
                        ")"
        );

        db.execSQL(
                "CREATE TABLE IF NOT EXISTS fees (" +
                        "id TEXT PRIMARY KEY," +
                        "student_id TEXT NOT NULL," +
                        "madrassa_id TEXT NOT NULL," +
                        "type TEXT NOT NULL," +
                        "amount REAL NOT NULL," +
                        "deadline TEXT," +
                        "status TEXT NOT NULL," +
                        "submitted_at INTEGER NOT NULL," +
                        "confirmed_at INTEGER NOT NULL," +
                        "locked_at INTEGER NOT NULL," +
                        "submitted_by TEXT," +
                        "confirmed_by TEXT" +
                        ")"
        );

        db.execSQL(
                "CREATE TABLE IF NOT EXISTS sms_messages (" +
                        "id TEXT PRIMARY KEY," +
                        "madrassa_id TEXT NOT NULL," +
                        "recipient TEXT NOT NULL," +
                        "message TEXT NOT NULL," +
                        "type TEXT NOT NULL," +
                        "provider TEXT," +
                        "status TEXT NOT NULL," +
                        "created_at INTEGER NOT NULL," +
                        "sent_at INTEGER NOT NULL," +
                        "delivered_at INTEGER NOT NULL" +
                        ")"
        );
        createAcademicSchema(db);
    }

    @Override
    public void onUpgrade(
            SQLiteDatabase db,
            int oldVersion,
            int newVersion
    ) {

        if (oldVersion < 2) {
            db.execSQL(
                    "CREATE TABLE IF NOT EXISTS madrassa_phones (" +
                            "id TEXT PRIMARY KEY," +
                            "madrassa_id TEXT NOT NULL," +
                            "phone TEXT NOT NULL," +
                            "label TEXT," +
                            "primary_phone INTEGER NOT NULL," +
                            "active INTEGER NOT NULL," +
                            "created_at INTEGER NOT NULL," +
                            "verified_at INTEGER NOT NULL" +
                            ")"
            );

            db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS idx_madrassa_phone_unique " +
                            "ON madrassa_phones(phone)"
            );

            db.execSQL(
                    "CREATE INDEX IF NOT EXISTS idx_madrassa_phones_madrassa " +
                            "ON madrassa_phones(madrassa_id)"
            );
        }

        if (oldVersion < 3) {
            db.execSQL(
                    "CREATE TABLE IF NOT EXISTS credentials (" +
                            "id TEXT PRIMARY KEY," +
                            "account_id TEXT NOT NULL," +
                            "role TEXT NOT NULL," +
                            "madrassa_id TEXT NOT NULL," +
                            "password_hash TEXT NOT NULL," +
                            "password_salt TEXT NOT NULL," +
                            "active INTEGER NOT NULL," +
                            "first_login INTEGER NOT NULL," +
                            "created_at INTEGER NOT NULL," +
                            "updated_at INTEGER NOT NULL," +
                            "last_login_at INTEGER NOT NULL" +
                            ")"
            );

            db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS idx_credentials_account_role " +
                            "ON credentials(account_id, role)"
            );

            db.execSQL(
                    "CREATE INDEX IF NOT EXISTS idx_credentials_madrassa " +
                            "ON credentials(madrassa_id)"
            );
        }

        if (oldVersion < 4) {
            db.execSQL(
                    "CREATE TABLE IF NOT EXISTS pending_registration_credentials (" +
                            "id TEXT PRIMARY KEY," +
                            "madrassa_id TEXT NOT NULL," +
                            "role TEXT NOT NULL," +
                            "password_hash TEXT NOT NULL," +
                            "password_salt TEXT NOT NULL," +
                            "created_at INTEGER NOT NULL," +
                            "updated_at INTEGER NOT NULL" +
                            ")"
            );

            db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS idx_pending_registration_madrassa_role " +
                            "ON pending_registration_credentials(madrassa_id, role)"
            );
        }

        if (oldVersion < 5) {

            addColumnIfMissing(
                    db,
                    "parents",
                    "madrassa_id",
                    "TEXT"
            );

            db.execSQL(
                    "CREATE INDEX IF NOT EXISTS idx_parents_madrassa " +
                            "ON parents(madrassa_id)"
            );

            db.execSQL(
                    "CREATE TABLE IF NOT EXISTS parent_student_links (" +
                            "parent_id TEXT NOT NULL," +
                            "student_id TEXT NOT NULL," +
                            "created_at INTEGER NOT NULL," +
                            "PRIMARY KEY(parent_id, student_id)" +
                            ")"
            );

            db.execSQL(
                    "CREATE INDEX IF NOT EXISTS idx_parent_student_parent " +
                            "ON parent_student_links(parent_id)"
            );

            db.execSQL(
                    "CREATE INDEX IF NOT EXISTS idx_parent_student_student " +
                            "ON parent_student_links(student_id)"
            );

            db.execSQL(
                    "CREATE TABLE IF NOT EXISTS student_programmes (" +
                            "student_id TEXT NOT NULL," +
                            "programme_id TEXT NOT NULL," +
                            "created_at INTEGER NOT NULL," +
                            "PRIMARY KEY(student_id, programme_id)" +
                            ")"
            );

            db.execSQL(
                    "CREATE INDEX IF NOT EXISTS idx_student_programmes_student " +
                            "ON student_programmes(student_id)"
            );

            db.execSQL(
                    "CREATE INDEX IF NOT EXISTS idx_student_programmes_programme " +
                            "ON student_programmes(programme_id)"
            );
        }

        if (oldVersion < 6) {
            db.execSQL(
                    "CREATE TABLE IF NOT EXISTS courses (" +
                            "id TEXT PRIMARY KEY," +
                            "madrassa_id TEXT," +
                            "programme_id TEXT NOT NULL," +
                            "name TEXT NOT NULL," +
                            "active INTEGER NOT NULL" +
                            ")"
            );
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_courses_madrassa ON courses(madrassa_id)");
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_courses_programme ON courses(programme_id)");

            db.execSQL(
                    "CREATE TABLE IF NOT EXISTS academic_units (" +
                            "id TEXT PRIMARY KEY," +
                            "madrassa_id TEXT," +
                            "course_id TEXT NOT NULL," +
                            "name TEXT NOT NULL," +
                            "order_index INTEGER NOT NULL" +
                            ")"
            );
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_academic_units_course ON academic_units(course_id)");

            db.execSQL(
                    "CREATE TABLE IF NOT EXISTS lessons (" +
                            "id TEXT PRIMARY KEY," +
                            "madrassa_id TEXT," +
                            "unit_id TEXT NOT NULL," +
                            "name TEXT NOT NULL," +
                            "content_type TEXT NOT NULL," +
                            "order_index INTEGER NOT NULL" +
                            ")"
            );
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_lessons_unit ON lessons(unit_id)");

            db.execSQL(
                    "CREATE TABLE IF NOT EXISTS learning_progress (" +
                            "id TEXT PRIMARY KEY," +
                            "madrassa_id TEXT," +
                            "learner_id TEXT NOT NULL," +
                            "lesson_id TEXT NOT NULL," +
                            "state TEXT NOT NULL," +
                            "updated_at INTEGER NOT NULL" +
                            ")"
            );
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_learning_progress_learner ON learning_progress(learner_id)");
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_learning_progress_lesson ON learning_progress(lesson_id)");
        }

        if (oldVersion < 7) {
            createAcademicSchema(db);
            db.execSQL("UPDATE learning_progress SET status = state WHERE status IS NULL");
        }

        if (oldVersion < 8) {
            migrateProgrammesToNullableMadrassa(db);
        }

        if (oldVersion < 9) {
            createContentShareSchema(db);
        }

        if (oldVersion < 10) {
            createStudentGroupSchema(db);
        }

        if (oldVersion < 10) {
            migrateClassGroupsToFlexibleSchema(db);
        }
    }

    private static void createAcademicSchema(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS courses (" +
                "id TEXT PRIMARY KEY, madrassa_id TEXT, programme_id TEXT NOT NULL, " +
                "name TEXT NOT NULL, level_number INTEGER NOT NULL DEFAULT 0, " +
                "active INTEGER NOT NULL DEFAULT 1)");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_courses_madrassa ON courses(madrassa_id)");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_courses_programme ON courses(programme_id)");

        db.execSQL("CREATE TABLE IF NOT EXISTS academic_units (" +
                "id TEXT PRIMARY KEY, madrassa_id TEXT, course_id TEXT NOT NULL, " +
                "name TEXT NOT NULL, order_index INTEGER NOT NULL DEFAULT 0)");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_academic_units_course ON academic_units(course_id)");

        db.execSQL("CREATE TABLE IF NOT EXISTS lessons (" +
                "id TEXT PRIMARY KEY, madrassa_id TEXT, unit_id TEXT NOT NULL, " +
                "name TEXT NOT NULL, content_type TEXT NOT NULL, " +
                "order_index INTEGER NOT NULL DEFAULT 0, active INTEGER NOT NULL DEFAULT 1)");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_lessons_unit ON lessons(unit_id)");

        db.execSQL("CREATE TABLE IF NOT EXISTS learning_materials (" +
                "id TEXT PRIMARY KEY, madrassa_id TEXT, programme_id TEXT NOT NULL, " +
                "title TEXT NOT NULL, author TEXT, type TEXT NOT NULL, content TEXT NOT NULL, " +
                "active INTEGER NOT NULL DEFAULT 1)");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_learning_materials_madrassa ON learning_materials(madrassa_id)");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_learning_materials_programme ON learning_materials(programme_id)");

        db.execSQL("CREATE TABLE IF NOT EXISTS assignments (" +
                "id TEXT PRIMARY KEY, madrassa_id TEXT, programme_id TEXT NOT NULL, " +
                "course_id TEXT, unit_id TEXT, lesson_id TEXT, title TEXT NOT NULL, " +
                "description TEXT, max_points REAL NOT NULL, due_date INTEGER NOT NULL, " +
                "active INTEGER NOT NULL DEFAULT 1)");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_assignments_madrassa ON assignments(madrassa_id)");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_assignments_programme ON assignments(programme_id)");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_assignments_lesson ON assignments(lesson_id)");

        db.execSQL("CREATE TABLE IF NOT EXISTS assessments (" +
                "id TEXT PRIMARY KEY, madrassa_id TEXT, student_id TEXT NOT NULL, " +
                "assignment_id TEXT NOT NULL, programme_id TEXT, score REAL NOT NULL, " +
                "feedback TEXT, status TEXT NOT NULL, recorded_at INTEGER NOT NULL, recorded_by TEXT)");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_assessments_madrassa ON assessments(madrassa_id)");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_assessments_student ON assessments(student_id)");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_assessments_assignment ON assessments(assignment_id)");

        db.execSQL("CREATE TABLE IF NOT EXISTS learning_progress (" +
                "id TEXT PRIMARY KEY, madrassa_id TEXT, learner_id TEXT NOT NULL, " +
                "lesson_id TEXT NOT NULL, status TEXT NOT NULL, updated_at INTEGER NOT NULL)");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_learning_progress_learner ON learning_progress(learner_id)");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_learning_progress_lesson ON learning_progress(lesson_id)");

        addColumnIfMissing(db, "courses", "level_number", "INTEGER NOT NULL DEFAULT 0");
        addColumnIfMissing(db, "lessons", "order_index", "INTEGER NOT NULL DEFAULT 0");
        addColumnIfMissing(db, "lessons", "active", "INTEGER NOT NULL DEFAULT 1");
        addColumnIfMissing(db, "learning_progress", "status", "TEXT");
    }

    private static void addColumnIfMissing(
            SQLiteDatabase db,
            String table,
            String column,
            String definition
    ) {
        boolean exists = false;

        Cursor cursor = db.rawQuery(
                "PRAGMA table_info(" + table + ")",
                null
        );

        try {
            int nameIndex = cursor.getColumnIndex("name");

            while (cursor.moveToNext()) {
                if (nameIndex >= 0
                        && column.equalsIgnoreCase(
                        cursor.getString(nameIndex)
                )) {
                    exists = true;
                    break;
                }
            }
        } finally {
            cursor.close();
        }

        if (!exists) {
            db.execSQL(
                    "ALTER TABLE " + table +
                            " ADD COLUMN " + column +
                            " " + definition
            );
        }
    }

    @Override
    public void onDowngrade(
            SQLiteDatabase db,
            int oldVersion,
            int newVersion
    ) {
        throw new IllegalStateException(
                "Database downgrade is not supported."
        );
    }

    private static void migrateProgrammesToNullableMadrassa(SQLiteDatabase db) {
        db.beginTransaction();

        try {
            db.execSQL(
                    "CREATE TABLE programmes_v8 (" +
                            "id TEXT PRIMARY KEY," +
                            "madrassa_id TEXT," +
                            "name TEXT NOT NULL," +
                            "category TEXT," +
                            "default_programme INTEGER NOT NULL," +
                            "active INTEGER NOT NULL" +
                            ")"
            );

            db.execSQL(
                    "INSERT INTO programmes_v8 " +
                            "(id, madrassa_id, name, category, default_programme, active) " +
                            "SELECT id, madrassa_id, name, category, " +
                            "default_programme, active " +
                            "FROM programmes"
            );

            db.execSQL("DROP TABLE programmes");

            db.execSQL(
                    "ALTER TABLE programmes_v8 RENAME TO programmes"
            );

            db.execSQL(
                    "CREATE INDEX IF NOT EXISTS idx_programmes_madrassa " +
                            "ON programmes(madrassa_id)"
            );

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }


    private static void createContentShareSchema(
            SQLiteDatabase db
    ) {
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS content_shares (" +
                        "id TEXT PRIMARY KEY," +
                        "madrassa_id TEXT NOT NULL," +
                        "content_type TEXT NOT NULL," +
                        "content_id TEXT NOT NULL," +
                        "scope TEXT NOT NULL," +
                        "target_id TEXT," +
                        "created_by TEXT," +
                        "active INTEGER NOT NULL DEFAULT 1," +
                        "created_at INTEGER NOT NULL," +
                        "updated_at INTEGER NOT NULL" +
                        ")"
        );

        db.execSQL(
                "CREATE INDEX IF NOT EXISTS " +
                        "idx_content_shares_madrassa " +
                        "ON content_shares(madrassa_id)"
        );

        db.execSQL(
                "CREATE INDEX IF NOT EXISTS " +
                        "idx_content_shares_content " +
                        "ON content_shares(content_type, content_id)"
        );

        db.execSQL(
                "CREATE INDEX IF NOT EXISTS " +
                        "idx_content_shares_target " +
                        "ON content_shares(" +
                        "madrassa_id, scope, target_id" +
                        ")"
        );
    }


    private static void createStudentGroupSchema(
            SQLiteDatabase db
    ) {
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS student_groups (" +
                        "id TEXT PRIMARY KEY," +
                        "madrassa_id TEXT NOT NULL," +
                        "name TEXT NOT NULL," +
                        "description TEXT," +
                        "active INTEGER NOT NULL DEFAULT 1," +
                        "created_at INTEGER NOT NULL," +
                        "updated_at INTEGER NOT NULL" +
                        ")"
        );

        db.execSQL(
                "CREATE INDEX IF NOT EXISTS " +
                        "idx_student_groups_madrassa " +
                        "ON student_groups(madrassa_id)"
        );

        db.execSQL(
                "CREATE TABLE IF NOT EXISTS " +
                        "student_group_members (" +
                        "group_id TEXT NOT NULL," +
                        "student_id TEXT NOT NULL," +
                        "created_at INTEGER NOT NULL," +
                        "PRIMARY KEY(group_id, student_id)" +
                        ")"
        );

        db.execSQL(
                "CREATE INDEX IF NOT EXISTS " +
                        "idx_student_group_members_group " +
                        "ON student_group_members(group_id)"
        );

        db.execSQL(
                "CREATE INDEX IF NOT EXISTS " +
                        "idx_student_group_members_student " +
                        "ON student_group_members(student_id)"
        );
    }


    private static void migrateClassGroupsToFlexibleSchema(
            SQLiteDatabase db
    ) {
        try {
            db.execSQL(
                    "ALTER TABLE class_groups " +
                            "ADD COLUMN code TEXT"
            );
        } catch (Exception ignored) {
            // Column may already exist.
        }

        try {
            db.execSQL(
                    "ALTER TABLE class_groups " +
                            "ADD COLUMN order_index INTEGER " +
                            "NOT NULL DEFAULT 0"
            );
        } catch (Exception ignored) {
            // Column may already exist.
        }
    }

}
