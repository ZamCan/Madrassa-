package com.zamcan.madrassa.data.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.zamcan.madrassa.data.local.EduNoorDatabase;
import com.zamcan.madrassa.data.model.Parent;
import com.zamcan.madrassa.domain.repository.ParentStore;

public final class ParentRepository implements ParentStore {

    private final EduNoorDatabase database;

    public ParentRepository(EduNoorDatabase database) {
        if (database == null) {
            throw new IllegalArgumentException(
                    "database is required"
            );
        }

        this.database = database;
    }

    @Override
    public Parent findById(String id) {
        if (isBlank(id)) {
            return null;
        }

        SQLiteDatabase db = database.getReadableDatabase();

        Cursor cursor = db.query(
                "parents",
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
    public Parent findByPhone(String phone) {
        if (isBlank(phone)) {
            return null;
        }

        SQLiteDatabase db = database.getReadableDatabase();

        Cursor cursor = db.query(
                "parents",
                null,
                "phone = ?",
                new String[]{normalizePhone(phone)},
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
    public boolean existsByPhone(String phone) {
        if (isBlank(phone)) {
            return false;
        }

        SQLiteDatabase db = database.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT 1 FROM parents WHERE phone = ? LIMIT 1",
                new String[]{normalizePhone(phone)}
        );

        try {
            return cursor.moveToFirst();
        } finally {
            cursor.close();
        }
    }

    @Override
    public void save(Parent parent) {
        validate(parent);

        SQLiteDatabase db = database.getWritableDatabase();

        long result = db.insertOrThrow(
                "parents",
                null,
                values(parent)
        );

        if (result == -1) {
            throw new IllegalStateException(
                    "Unable to save parent."
            );
        }
    }

    @Override
    public void update(Parent parent) {
        validate(parent);

        SQLiteDatabase db = database.getWritableDatabase();

        int affected = db.update(
                "parents",
                values(parent),
                "id = ?",
                new String[]{parent.id.trim()}
        );

        if (affected != 1) {
            throw new IllegalStateException(
                    "Parent update affected " +
                            affected +
                            " rows."
            );
        }
    }

    private Parent map(
            Cursor cursor,
            SQLiteDatabase db
    ) {
        Parent parent = new Parent();

        parent.id = text(cursor, "id");
        parent.madrassaId = text(cursor, "madrassa_id");
        parent.fullName = text(cursor, "full_name");
        parent.phone = text(cursor, "phone");
        parent.firstLogin = integer(
                cursor,
                "first_login"
        ) == 1;
        parent.active = integer(
                cursor,
                "active"
        ) == 1;

        Cursor links = db.rawQuery(
                "SELECT student_id " +
                        "FROM parent_student_links " +
                        "WHERE parent_id = ? " +
                        "ORDER BY student_id ASC",
                new String[]{parent.id}
        );

        try {
            while (links.moveToNext()) {
                parent.linkedStudentIds.add(
                        links.getString(0)
                );
            }
        } finally {
            links.close();
        }

        return parent;
    }

    private ContentValues values(Parent parent) {
        ContentValues values = new ContentValues();

        values.put("id", parent.id.trim());
        values.put(
                "madrassa_id",
                blankToNull(parent.madrassaId)
        );
        values.put(
                "full_name",
                parent.fullName.trim()
        );
        values.put(
                "phone",
                normalizePhone(parent.phone)
        );
        values.put(
                "first_login",
                parent.firstLogin ? 1 : 0
        );
        values.put(
                "active",
                parent.active ? 1 : 0
        );

        return values;
    }

    private void validate(Parent parent) {
        if (parent == null) {
            throw new IllegalArgumentException(
                    "parent is required"
            );
        }

        require(parent.id, "parent.id");
        require(parent.fullName, "parent.fullName");
        require(parent.phone, "parent.phone");
    }

    private static String normalizePhone(String phone) {
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

    private static String blankToNull(String value) {
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

    private static boolean isBlank(String value) {
        return value == null
                || value.trim().isEmpty();
    }
}
