package com.zamcan.madrassa.data.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.zamcan.madrassa.data.local.EduNoorDatabase;
import com.zamcan.madrassa.data.model.ClassGroup;
import com.zamcan.madrassa.domain.repository.ClassStore;

import java.util.ArrayList;
import java.util.List;

public final class ClassRepository implements ClassStore {

    private final EduNoorDatabase database;

    public ClassRepository(EduNoorDatabase database) {
        if (database == null) {
            throw new IllegalArgumentException(
                    "database is required"
            );
        }

        this.database = database;
    }

    @Override
    public ClassGroup findById(String classId) {
        if (blank(classId)) {
            return null;
        }

        Cursor cursor = database.getReadableDatabase().query(
                "class_groups",
                null,
                "id = ?",
                new String[]{classId.trim()},
                null,
                null,
                null,
                "1"
        );

        try {
            return cursor.moveToFirst()
                    ? map(cursor)
                    : null;
        } finally {
            cursor.close();
        }
    }

    @Override
    public List<ClassGroup> findByMadrassa(
            String madrassaId
    ) {
        return find(
                "madrassa_id = ?",
                new String[]{trimmedRequired(madrassaId)}
        );
    }

    @Override
    public List<ClassGroup> findActiveByMadrassa(
            String madrassaId
    ) {
        if (blank(madrassaId)) {
            return new ArrayList<>();
        }

        return find(
                "madrassa_id = ? AND active = 1",
                new String[]{madrassaId.trim()}
        );
    }

    @Override
    public boolean save(ClassGroup classGroup) {
        validate(classGroup);

        return database.getWritableDatabase().insert(
                "class_groups",
                null,
                values(classGroup)
        ) != -1;
    }

    @Override
    public boolean update(ClassGroup classGroup) {
        validate(classGroup);

        return database.getWritableDatabase().update(
                "class_groups",
                values(classGroup),
                "id = ?",
                new String[]{classGroup.id.trim()}
        ) == 1;
    }

    private List<ClassGroup> find(
            String selection,
            String[] args
    ) {
        List<ClassGroup> result = new ArrayList<>();

        if (args.length == 0 || blank(args[0])) {
            return result;
        }

        Cursor cursor = database.getReadableDatabase().query(
                "class_groups",
                null,
                selection,
                args,
                null,
                null,
                "order_index ASC, name ASC"
        );

        try {
            while (cursor.moveToNext()) {
                result.add(map(cursor));
            }
        } finally {
            cursor.close();
        }

        return result;
    }

    private ClassGroup map(Cursor cursor) {
        ClassGroup value = new ClassGroup();

        value.id = cursor.getString(
                cursor.getColumnIndexOrThrow("id")
        );

        value.madrassaId = cursor.getString(
                cursor.getColumnIndexOrThrow("madrassa_id")
        );

        value.code = text(cursor, "code");

        value.name = cursor.getString(
                cursor.getColumnIndexOrThrow("name")
        );

        value.teacherId = text(cursor, "teacher_id");

        value.orderIndex = cursor.getInt(
                cursor.getColumnIndexOrThrow("order_index")
        );

        value.active = cursor.getInt(
                cursor.getColumnIndexOrThrow("active")
        ) != 0;

        return value;
    }

    private ContentValues values(
            ClassGroup value
    ) {
        ContentValues values = new ContentValues();

        values.put("id", value.id.trim());
        values.put(
                "madrassa_id",
                value.madrassaId.trim()
        );

        if (blank(value.code)) {
            values.putNull("code");
        } else {
            values.put("code", value.code.trim());
        }

        values.put("name", value.name.trim());

        if (blank(value.teacherId)) {
            values.putNull("teacher_id");
        } else {
            values.put(
                    "teacher_id",
                    value.teacherId.trim()
            );
        }

        values.put("order_index", value.orderIndex);
        values.put("active", value.active ? 1 : 0);

        return values;
    }

    private static void validate(ClassGroup value) {
        if (value == null
                || blank(value.id)
                || blank(value.madrassaId)
                || blank(value.name)) {
            throw new IllegalArgumentException(
                    "class data is incomplete"
            );
        }
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

    private static String trimmedRequired(
            String value
    ) {
        if (blank(value)) {
            return "";
        }

        return value.trim();
    }

    private static boolean blank(String value) {
        return value == null
                || value.trim().isEmpty();
    }
}
