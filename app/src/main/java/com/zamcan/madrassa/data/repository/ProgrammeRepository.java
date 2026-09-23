package com.zamcan.madrassa.data.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.zamcan.madrassa.data.local.EduNoorDatabase;
import com.zamcan.madrassa.data.model.Programme;
import com.zamcan.madrassa.domain.repository.ProgrammeStore;

import java.util.ArrayList;
import java.util.List;

public final class ProgrammeRepository implements ProgrammeStore {

    private final EduNoorDatabase database;

    public ProgrammeRepository(EduNoorDatabase database) {
        if (database == null) {
            throw new IllegalArgumentException("database is required");
        }
        this.database = database;
    }

    @Override
    public Programme findById(String programmeId) {
        if (blank(programmeId)) {
            return null;
        }

        Cursor cursor = database.getReadableDatabase().query(
                "programmes",
                null,
                "id = ?",
                new String[]{programmeId.trim()},
                null,
                null,
                null,
                "1"
        );

        try {
            return cursor.moveToFirst() ? map(cursor) : null;
        } finally {
            cursor.close();
        }
    }

    @Override
    public List<Programme> findGlobal() {
        return find(
                "madrassa_id IS NULL",
                new String[]{}
        );
    }

    @Override
    public List<Programme> findActiveGlobal() {
        return find(
                "madrassa_id IS NULL AND active = 1",
                new String[]{}
        );
    }

    @Override
    public List<Programme> findByMadrassa(String madrassaId) {
        return find(
                "madrassa_id = ?",
                new String[]{trimmedRequired(madrassaId)}
        );
    }

    @Override
    public List<Programme> findActiveByMadrassa(String madrassaId) {
        if (blank(madrassaId)) {
            return new ArrayList<>();
        }

        return find(
                "madrassa_id = ? AND active = 1",
                new String[]{madrassaId.trim()}
        );
    }

    @Override
    public boolean save(Programme programme) {
        validate(programme);

        SQLiteDatabase db = database.getWritableDatabase();

        return db.insert(
                "programmes",
                null,
                values(programme)
        ) != -1;
    }

    @Override
    public boolean update(Programme programme) {
        validate(programme);

        SQLiteDatabase db = database.getWritableDatabase();

        return db.update(
                "programmes",
                values(programme),
                "id = ?",
                new String[]{programme.id.trim()}
        ) == 1;
    }

    private List<Programme> find(
            String selection,
            String[] selectionArgs
    ) {
        List<Programme> result = new ArrayList<>();

        if (selectionArgs.length == 0
                || blank(selectionArgs[0])) {
            return result;
        }

        Cursor cursor = database.getReadableDatabase().query(
                "programmes",
                null,
                selection,
                selectionArgs,
                null,
                null,
                "default_programme DESC, name ASC"
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

    private Programme map(Cursor cursor) {
        Programme programme = new Programme();

        programme.id =
                cursor.getString(
                        cursor.getColumnIndexOrThrow("id")
                );

        programme.madrassaId =
                cursor.getString(
                        cursor.getColumnIndexOrThrow("madrassa_id")
                );

        programme.name =
                cursor.getString(
                        cursor.getColumnIndexOrThrow("name")
                );

        programme.category =
                text(cursor, "category");

        programme.defaultProgramme =
                cursor.getInt(
                        cursor.getColumnIndexOrThrow(
                                "default_programme"
                        )
                ) != 0;

        programme.active =
                cursor.getInt(
                        cursor.getColumnIndexOrThrow("active")
                ) != 0;

        return programme;
    }

    private ContentValues values(Programme programme) {
        ContentValues values = new ContentValues();

        values.put(
                "id",
                programme.id.trim()
        );

        if (blank(programme.madrassaId)) {
            values.putNull("madrassa_id");
        } else {
            values.put(
                    "madrassa_id",
                    programme.madrassaId.trim()
            );
        }

        values.put(
                "name",
                programme.name.trim()
        );

        if (programme.category == null) {
            values.putNull("category");
        } else {
            values.put(
                    "category",
                    programme.category.trim()
            );
        }

        values.put(
                "default_programme",
                programme.defaultProgramme ? 1 : 0
        );

        values.put(
                "active",
                programme.active ? 1 : 0
        );

        return values;
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

    private static String trimmedRequired(String value) {
        if (blank(value)) {
            return "";
        }

        return value.trim();
    }

    private static boolean blank(String value) {
        return value == null
                || value.trim().isEmpty();
    }

    private static void validate(Programme programme) {
        if (programme == null
                || blank(programme.id)
                || blank(programme.name)) {
            throw new IllegalArgumentException(
                    "programme data is incomplete"
            );
        }
    }
}
