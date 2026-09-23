package com.zamcan.madrassa.data.repository;

import android.content.ContentValues;
import android.database.Cursor;

import com.zamcan.madrassa.data.local.EduNoorDatabase;
import com.zamcan.madrassa.data.model.StudentGroup;
import com.zamcan.madrassa.domain.repository.StudentGroupStore;

import java.util.ArrayList;
import java.util.List;

public final class StudentGroupRepository
        implements StudentGroupStore {

    private final EduNoorDatabase database;

    public StudentGroupRepository(
            EduNoorDatabase database
    ) {
        if (database == null) {
            throw new IllegalArgumentException(
                    "database is required"
            );
        }

        this.database = database;
    }

    @Override
    public StudentGroup findById(String groupId) {
        if (blank(groupId)) {
            return null;
        }

        Cursor cursor = database.getReadableDatabase().query(
                "student_groups",
                null,
                "id = ?",
                new String[]{groupId.trim()},
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
    public List<StudentGroup> findByMadrassa(
            String madrassaId
    ) {
        return find(
                "madrassa_id = ?",
                new String[]{trimmedRequired(madrassaId)}
        );
    }

    @Override
    public List<StudentGroup> findActiveByMadrassa(
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
    public boolean save(StudentGroup group) {
        validate(group);

        return database.getWritableDatabase().insert(
                "student_groups",
                null,
                values(group)
        ) != -1;
    }

    @Override
    public boolean update(StudentGroup group) {
        validate(group);

        return database.getWritableDatabase().update(
                "student_groups",
                values(group),
                "id = ?",
                new String[]{group.id.trim()}
        ) == 1;
    }

    private List<StudentGroup> find(
            String selection,
            String[] args
    ) {
        List<StudentGroup> result = new ArrayList<>();

        if (args.length == 0 || blank(args[0])) {
            return result;
        }

        Cursor cursor = database.getReadableDatabase().query(
                "student_groups",
                null,
                selection,
                args,
                null,
                null,
                "name ASC"
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

    private StudentGroup map(Cursor cursor) {
        StudentGroup group = new StudentGroup();

        group.id = cursor.getString(
                cursor.getColumnIndexOrThrow("id")
        );

        group.madrassaId = cursor.getString(
                cursor.getColumnIndexOrThrow("madrassa_id")
        );

        group.name = cursor.getString(
                cursor.getColumnIndexOrThrow("name")
        );

        group.description = text(
                cursor,
                "description"
        );

        group.active = cursor.getInt(
                cursor.getColumnIndexOrThrow("active")
        ) != 0;

        group.createdAt = cursor.getLong(
                cursor.getColumnIndexOrThrow("created_at")
        );

        group.updatedAt = cursor.getLong(
                cursor.getColumnIndexOrThrow("updated_at")
        );

        return group;
    }

    private ContentValues values(
            StudentGroup group
    ) {
        ContentValues values = new ContentValues();

        values.put("id", group.id.trim());
        values.put(
                "madrassa_id",
                group.madrassaId.trim()
        );
        values.put("name", group.name.trim());

        if (blank(group.description)) {
            values.putNull("description");
        } else {
            values.put(
                    "description",
                    group.description.trim()
            );
        }

        values.put("active", group.active ? 1 : 0);
        values.put("created_at", group.createdAt);
        values.put("updated_at", group.updatedAt);

        return values;
    }

    private static void validate(StudentGroup group) {
        if (group == null
                || blank(group.id)
                || blank(group.madrassaId)
                || blank(group.name)) {
            throw new IllegalArgumentException(
                    "student group data is incomplete"
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
        return blank(value) ? "" : value.trim();
    }

    private static boolean blank(String value) {
        return value == null
                || value.trim().isEmpty();
    }
}
