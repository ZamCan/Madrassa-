package com.zamcan.madrassa.data.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.zamcan.madrassa.data.local.EduNoorDatabase;
import com.zamcan.madrassa.data.model.ContentShare;
import com.zamcan.madrassa.domain.repository.ContentShareStore;

import java.util.ArrayList;
import java.util.List;

public final class ContentShareRepository implements ContentShareStore {

    private final EduNoorDatabase database;

    public ContentShareRepository(EduNoorDatabase database) {
        if (database == null) {
            throw new IllegalArgumentException("database is required");
        }

        this.database = database;
    }

    @Override
    public ContentShare findById(String id) {
        if (blank(id)) {
            return null;
        }

        Cursor cursor = database.getReadableDatabase().query(
                "content_shares",
                null,
                "id = ?",
                new String[]{id.trim()},
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
    public List<ContentShare> findByContent(
            String contentType,
            String contentId
    ) {
        if (blank(contentType) || blank(contentId)) {
            return new ArrayList<>();
        }

        return find(
                "content_type = ? AND content_id = ?",
                new String[]{
                        contentType.trim(),
                        contentId.trim()
                }
        );
    }

    @Override
    public List<ContentShare> findByMadrassa(
            String madrassaId
    ) {
        if (blank(madrassaId)) {
            return new ArrayList<>();
        }

        return find(
                "madrassa_id = ?",
                new String[]{madrassaId.trim()}
        );
    }

    @Override
    public List<ContentShare> findActiveForStudent(
            String madrassaId,
            String studentId
    ) {
        if (blank(madrassaId) || blank(studentId)) {
            return new ArrayList<>();
        }

        /*
         * Student-specific shares are resolved directly here.
         *
         * Madrassa-wide and GROUP shares are intentionally not
         * expanded here yet. Group membership belongs to the
         * next access-resolution layer.
         */
        return find(
                "madrassa_id = ? " +
                        "AND active = 1 " +
                        "AND scope = ? " +
                        "AND target_id = ?",
                new String[]{
                        madrassaId.trim(),
                        ContentShare.SCOPE_STUDENT,
                        studentId.trim()
                }
        );
    }

    @Override
    public boolean save(ContentShare share) {
        validate(share);

        SQLiteDatabase db = database.getWritableDatabase();

        return db.insert(
                "content_shares",
                null,
                values(share)
        ) != -1;
    }

    @Override
    public boolean update(ContentShare share) {
        validate(share);

        SQLiteDatabase db = database.getWritableDatabase();

        return db.update(
                "content_shares",
                values(share),
                "id = ?",
                new String[]{share.id.trim()}
        ) == 1;
    }

    private List<ContentShare> find(
            String selection,
            String[] args
    ) {
        List<ContentShare> result = new ArrayList<>();

        Cursor cursor = database.getReadableDatabase().query(
                "content_shares",
                null,
                selection,
                args,
                null,
                null,
                "created_at DESC"
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

    private ContentShare map(Cursor cursor) {
        ContentShare share = new ContentShare();

        share.id = cursor.getString(
                cursor.getColumnIndexOrThrow("id")
        );

        share.madrassaId = cursor.getString(
                cursor.getColumnIndexOrThrow("madrassa_id")
        );

        share.contentType = cursor.getString(
                cursor.getColumnIndexOrThrow("content_type")
        );

        share.contentId = cursor.getString(
                cursor.getColumnIndexOrThrow("content_id")
        );

        share.scope = cursor.getString(
                cursor.getColumnIndexOrThrow("scope")
        );

        share.targetId = text(cursor, "target_id");

        share.createdBy = text(cursor, "created_by");

        share.active = cursor.getInt(
                cursor.getColumnIndexOrThrow("active")
        ) != 0;

        share.createdAt = cursor.getLong(
                cursor.getColumnIndexOrThrow("created_at")
        );

        share.updatedAt = cursor.getLong(
                cursor.getColumnIndexOrThrow("updated_at")
        );

        return share;
    }

    private ContentValues values(ContentShare share) {
        ContentValues values = new ContentValues();

        values.put("id", share.id.trim());
        values.put("madrassa_id", share.madrassaId.trim());
        values.put("content_type", share.contentType.trim());
        values.put("content_id", share.contentId.trim());
        values.put("scope", share.scope.trim());

        if (blank(share.targetId)) {
            values.putNull("target_id");
        } else {
            values.put("target_id", share.targetId.trim());
        }

        if (blank(share.createdBy)) {
            values.putNull("created_by");
        } else {
            values.put("created_by", share.createdBy.trim());
        }

        values.put("active", share.active ? 1 : 0);
        values.put("created_at", share.createdAt);
        values.put("updated_at", share.updatedAt);

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

    private static void validate(ContentShare share) {
        if (share == null
                || blank(share.id)
                || blank(share.madrassaId)
                || blank(share.contentType)
                || blank(share.contentId)
                || blank(share.scope)) {
            throw new IllegalArgumentException(
                    "content share data is incomplete"
            );
        }

        if (!ContentShare.SCOPE_MADRASSA.equals(share.scope)
                && !ContentShare.SCOPE_STUDENT.equals(share.scope)
                && !ContentShare.SCOPE_GROUP.equals(share.scope)) {
            throw new IllegalArgumentException(
                    "unsupported content share scope"
            );
        }

        if (ContentShare.SCOPE_MADRASSA.equals(share.scope)
                && !blank(share.targetId)) {
            throw new IllegalArgumentException(
                    "madrassa share cannot have targetId"
            );
        }

        if (!ContentShare.SCOPE_MADRASSA.equals(share.scope)
                && blank(share.targetId)) {
            throw new IllegalArgumentException(
                    "targetId is required for targeted share"
            );
        }
    }

    private static boolean blank(String value) {
        return value == null
                || value.trim().isEmpty();
    }
}
