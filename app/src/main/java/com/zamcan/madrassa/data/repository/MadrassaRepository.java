package com.zamcan.madrassa.data.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.zamcan.madrassa.data.local.EduNoorDatabase;
import com.zamcan.madrassa.data.model.ApprovalStatus;
import com.zamcan.madrassa.data.model.Madrassa;
import com.zamcan.madrassa.domain.repository.MadrassaStore;

import java.util.ArrayList;
import java.util.List;

public class MadrassaRepository implements MadrassaStore {

    private final EduNoorDatabase database;

    public MadrassaRepository(Context context) {
        database = new EduNoorDatabase(context);
    }

    public boolean save(Madrassa madrassa) {

        if (madrassa == null ||
                madrassa.id == null ||
                madrassa.name == null) {
            return false;
        }

        SQLiteDatabase db = database.getWritableDatabase();

        ContentValues values = toValues(madrassa);

        long result = db.insertWithOnConflict(
                "madrassas",
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
        );

        return result != -1;
    }

    @Override
    public boolean existsById(String id) {

        if (id == null || id.trim().isEmpty()) {
            return false;
        }

        SQLiteDatabase db =
                database.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT 1 FROM madrassas WHERE id = ? LIMIT 1",
                new String[]{id}
        );

        try {
            return cursor.moveToFirst();
        } finally {
            cursor.close();
        }
    }

    @Override
    public boolean existsByName(String name) {

        if (name == null ||
                name.trim().isEmpty()) {
            return false;
        }

        SQLiteDatabase db =
                database.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT 1 FROM madrassas " +
                "WHERE name = ? COLLATE NOCASE LIMIT 1",
                new String[]{name.trim()}
        );

        try {
            return cursor.moveToFirst();
        } finally {
            cursor.close();
        }
    }


    @Override
    public Madrassa findByName(String name) {

        if (name == null || name.trim().isEmpty()) {
            return null;
        }

        SQLiteDatabase db =
                database.getReadableDatabase();

        Cursor cursor = db.query(
                "madrassas",
                null,
                "name = ? COLLATE NOCASE",
                new String[]{name.trim()},
                null,
                null,
                "id ASC",
                "1"
        );

        try {
            if (!cursor.moveToFirst()) {
                return null;
            }

            return fromCursor(cursor);
        } finally {
            cursor.close();
        }
    }


    public Madrassa findById(String id) {

        if (id == null || id.trim().isEmpty()) {
            return null;
        }

        SQLiteDatabase db = database.getReadableDatabase();

        Cursor cursor = db.query(
                "madrassas",
                null,
                "id = ?",
                new String[]{id},
                null,
                null,
                null
        );

        try {
            if (cursor.moveToFirst()) {
                return fromCursor(cursor);
            }

            return null;

        } finally {
            cursor.close();
        }
    }

    public List<Madrassa> findAll() {

        SQLiteDatabase db = database.getReadableDatabase();

        Cursor cursor = db.query(
                "madrassas",
                null,
                null,
                null,
                null,
                null,
                "name COLLATE NOCASE ASC"
        );

        List<Madrassa> result = new ArrayList<>();

        try {
            while (cursor.moveToNext()) {
                result.add(fromCursor(cursor));
            }

            return result;

        } finally {
            cursor.close();
        }
    }

    public boolean updateApprovalStatus(
            String id,
            ApprovalStatus status,
            String rejectionReason
    ) {

        if (id == null ||
                id.trim().isEmpty() ||
                status == null) {
            return false;
        }

        SQLiteDatabase db = database.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(
                "approval_status",
                status.name()
        );

        values.put(
                "rejection_reason",
                rejectionReason
        );

        if (status == ApprovalStatus.APPROVED ||
                status == ApprovalStatus.ACTIVE) {

            values.put(
                    "approved_at",
                    System.currentTimeMillis()
            );
        }

        int rows = db.update(
                "madrassas",
                values,
                "id = ?",
                new String[]{id}
        );

        return rows > 0;
    }

    private ContentValues toValues(Madrassa madrassa) {

        ContentValues values = new ContentValues();

        values.put("id", madrassa.id);
        values.put("name", madrassa.name);
        values.put("type", madrassa.type);
        values.put(
                "administration_type",
                madrassa.administrationType
        );

        values.put("region", madrassa.region);
        values.put("district", madrassa.district);
        values.put("ward", madrassa.ward);
        values.put("area", madrassa.area);
        values.put(
                "nearby_landmark",
                madrassa.nearbyLandmark
        );

        values.put(
                "masjid_name",
                madrassa.masjidName
        );

        values.put(
                "masjid_location",
                madrassa.masjidLocation
        );

        values.put(
                "head_ustadh_id",
                madrassa.headUstadhId
        );

        values.put("phone", madrassa.phone);
        values.put(
                "secondary_phone",
                madrassa.secondaryPhone
        );

        values.put("email", madrassa.email);
        values.put(
                "ustadh_count",
                madrassa.ustadhCount
        );

        ApprovalStatus status =
                madrassa.approvalStatus;

        values.put(
                "approval_status",
                status == null
                        ? ApprovalStatus.DRAFT.name()
                        : status.name()
        );

        values.put(
                "rejection_reason",
                madrassa.rejectionReason
        );

        values.put(
                "submitted_at",
                madrassa.submittedAt
        );

        values.put(
                "approved_at",
                madrassa.approvedAt
        );

        return values;
    }

    private Madrassa fromCursor(Cursor cursor) {

        Madrassa madrassa = new Madrassa();

        madrassa.id = getString(cursor, "id");
        madrassa.name = getString(cursor, "name");
        madrassa.type = getString(cursor, "type");

        madrassa.administrationType =
                getString(
                        cursor,
                        "administration_type"
                );

        madrassa.region =
                getString(cursor, "region");

        madrassa.district =
                getString(cursor, "district");

        madrassa.ward =
                getString(cursor, "ward");

        madrassa.area =
                getString(cursor, "area");

        madrassa.nearbyLandmark =
                getString(
                        cursor,
                        "nearby_landmark"
                );

        madrassa.masjidName =
                getString(
                        cursor,
                        "masjid_name"
                );

        madrassa.masjidLocation =
                getString(
                        cursor,
                        "masjid_location"
                );

        madrassa.headUstadhId =
                getString(
                        cursor,
                        "head_ustadh_id"
                );

        madrassa.phone =
                getString(cursor, "phone");

        madrassa.secondaryPhone =
                getString(
                        cursor,
                        "secondary_phone"
                );

        madrassa.email =
                getString(cursor, "email");

        madrassa.ustadhCount =
                cursor.getInt(
                        cursor.getColumnIndexOrThrow(
                                "ustadh_count"
                        )
                );

        String status =
                getString(
                        cursor,
                        "approval_status"
                );

        try {
            madrassa.approvalStatus =
                    status == null
                            ? ApprovalStatus.DRAFT
                            : ApprovalStatus.valueOf(
                                    status
                            );
        } catch (IllegalArgumentException e) {
            madrassa.approvalStatus =
                    ApprovalStatus.DRAFT;
        }

        madrassa.rejectionReason =
                getString(
                        cursor,
                        "rejection_reason"
                );

        madrassa.submittedAt =
                cursor.getLong(
                        cursor.getColumnIndexOrThrow(
                                "submitted_at"
                        )
                );

        madrassa.approvedAt =
                cursor.getLong(
                        cursor.getColumnIndexOrThrow(
                                "approved_at"
                        )
                );

        return madrassa;
    }

    private String getString(
            Cursor cursor,
            String column
    ) {

        int index =
                cursor.getColumnIndex(column);

        if (index < 0 ||
                cursor.isNull(index)) {
            return null;
        }

        return cursor.getString(index);
    }

    public void close() {
        database.close();
    }
}
