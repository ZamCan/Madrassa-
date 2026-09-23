package com.zamcan.madrassa.data.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.zamcan.madrassa.data.local.EduNoorDatabase;
import com.zamcan.madrassa.data.model.Student;
import com.zamcan.madrassa.domain.repository.StudentStore;

import java.util.ArrayList;
import java.util.List;

public final class StudentRepository implements StudentStore {

    private final EduNoorDatabase database;

    public StudentRepository(EduNoorDatabase database) {
        if (database == null) {
            throw new IllegalArgumentException(
                    "database is required"
            );
        }

        this.database = database;
    }

    @Override
    public Student findById(String id) {
        if (isBlank(id)) {
            return null;
        }

        SQLiteDatabase db = database.getReadableDatabase();

        Cursor cursor = db.query(
                "students",
                null,
                "id = ?",
                new String[]{id.trim()},
                null,
                null,
                null,
                "1"
        );

        try {
            if (!cursor.moveToFirst()) {
                return null;
            }

            return map(cursor, db);
        } finally {
            cursor.close();
        }
    }

    @Override
    public List<Student> findByMadrassa(
            String madrassaId
    ) {
        List<Student> result = new ArrayList<>();

        if (isBlank(madrassaId)) {
            return result;
        }

        SQLiteDatabase db = database.getReadableDatabase();

        Cursor cursor = db.query(
                "students",
                null,
                "madrassa_id = ?",
                new String[]{madrassaId.trim()},
                null,
                null,
                "full_name ASC"
        );

        try {
            while (cursor.moveToNext()) {
                result.add(map(cursor, db));
            }
        } finally {
            cursor.close();
        }

        return result;
    }

    @Override
    public List<Student> findByParent(
            String parentId
    ) {
        List<Student> result = new ArrayList<>();

        if (isBlank(parentId)) {
            return result;
        }

        SQLiteDatabase db = database.getReadableDatabase();

        Cursor cursor = db.query(
                "students",
                null,
                "parent_id = ?",
                new String[]{parentId.trim()},
                null,
                null,
                "full_name ASC"
        );

        try {
            while (cursor.moveToNext()) {
                result.add(map(cursor, db));
            }
        } finally {
            cursor.close();
        }

        return result;
    }

    @Override
    public void save(Student student) {
        validate(student);

        SQLiteDatabase db = database.getWritableDatabase();

        db.insertOrThrow(
                "students",
                null,
                values(student)
        );

        saveProgrammeLinks(db, student);
    }

    @Override
    public void update(Student student) {
        validate(student);

        SQLiteDatabase db = database.getWritableDatabase();

        int affected = db.update(
                "students",
                values(student),
                "id = ?",
                new String[]{student.id.trim()}
        );

        if (affected != 1) {
            throw new IllegalStateException(
                    "Student update affected " +
                            affected +
                            " rows."
            );
        }

        db.delete(
                "student_programmes",
                "student_id = ?",
                new String[]{student.id.trim()}
        );

        saveProgrammeLinks(db, student);
    }

    private Student map(
            Cursor cursor,
            SQLiteDatabase db
    ) {
        Student student = new Student();

        student.id = text(cursor, "id");
        student.madrassaId =
                text(cursor, "madrassa_id");
        student.parentId =
                text(cursor, "parent_id");
        student.fullName =
                text(cursor, "full_name");
        student.parentGuardianName =
                text(cursor, "parent_guardian_name");
        student.mainPhone =
                text(cursor, "main_phone");
        student.emergencyPhone =
                text(cursor, "emergency_phone");
        student.classId =
                text(cursor, "class_id");
        student.quranLevel =
                text(cursor, "quran_level");
        student.hifzLevel =
                text(cursor, "hifz_level");
        student.active =
                integer(cursor, "active") == 1;

        Cursor programmes = db.rawQuery(
                "SELECT programme_id " +
                        "FROM student_programmes " +
                        "WHERE student_id = ? " +
                        "ORDER BY programme_id ASC",
                new String[]{student.id}
        );

        try {
            while (programmes.moveToNext()) {
                student.programmeIds.add(
                        programmes.getString(0)
                );
            }
        } finally {
            programmes.close();
        }

        return student;
    }

    private ContentValues values(Student student) {
        ContentValues values = new ContentValues();

        values.put("id", student.id.trim());
        values.put(
                "madrassa_id",
                student.madrassaId.trim()
        );
        values.put(
                "parent_id",
                blankToNull(student.parentId)
        );
        values.put(
                "full_name",
                student.fullName.trim()
        );
        values.put(
                "parent_guardian_name",
                student.parentGuardianName.trim()
        );
        values.put(
                "main_phone",
                normalizePhone(student.mainPhone)
        );
        values.put(
                "emergency_phone",
                blankToNull(
                        normalizePhone(
                                student.emergencyPhone
                        )
                )
        );
        values.put(
                "class_id",
                student.classId.trim()
        );
        values.put(
                "quran_level",
                student.quranLevel.trim()
        );
        values.put(
                "hifz_level",
                student.hifzLevel.trim()
        );
        values.put(
                "active",
                student.active ? 1 : 0
        );

        return values;
    }

    private void saveProgrammeLinks(
            SQLiteDatabase db,
            Student student
    ) {
        if (student.programmeIds == null) {
            return;
        }

        long now = System.currentTimeMillis();

        for (String programmeId :
                student.programmeIds) {

            if (isBlank(programmeId)) {
                continue;
            }

            ContentValues values =
                    new ContentValues();

            values.put(
                    "student_id",
                    student.id.trim()
            );

            values.put(
                    "programme_id",
                    programmeId.trim()
            );

            values.put(
                    "created_at",
                    now
            );

            db.insertOrThrow(
                    "student_programmes",
                    null,
                    values
            );
        }
    }

    private void validate(Student student) {
        if (student == null) {
            throw new IllegalArgumentException(
                    "student is required"
            );
        }

        require(student.id, "student.id");
        require(
                student.madrassaId,
                "student.madrassaId"
        );
        require(
                student.fullName,
                "student.fullName"
        );
        require(
                student.parentGuardianName,
                "student.parentGuardianName"
        );
        require(
                student.mainPhone,
                "student.mainPhone"
        );
        require(
                student.classId,
                "student.classId"
        );
        require(
                student.quranLevel,
                "student.quranLevel"
        );
        require(
                student.hifzLevel,
                "student.hifzLevel"
        );
    }

    private static String normalizePhone(
            String phone
    ) {
        if (isBlank(phone)) {
            return null;
        }

        String value = phone.trim()
                .replace(" ", "")
                .replace("-", "")
                .replace("(", "")
                .replace(")", "")
                .replace(".", "");

        if (value.startsWith("00")) {
            value = "+" + value.substring(2);
        }

        return value;
    }

    private static String text(
            Cursor cursor,
            String column
    ) {
        int index = cursor.getColumnIndex(column);

        if (index < 0 || cursor.isNull(index)) {
            return null;
        }

        return cursor.getString(index);
    }

    private static int integer(
            Cursor cursor,
            String column
    ) {
        return cursor.getInt(
                cursor.getColumnIndexOrThrow(column)
        );
    }

    private static String blankToNull(
            String value
    ) {
        if (isBlank(value)) {
            return null;
        }

        return value.trim();
    }

    private static void require(
            String value,
            String field
    ) {
        if (isBlank(value)) {
            throw new IllegalArgumentException(
                    field + " is required"
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
