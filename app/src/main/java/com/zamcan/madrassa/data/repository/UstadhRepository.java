package com.zamcan.madrassa.data.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.zamcan.madrassa.data.local.EduNoorDatabase;
import com.zamcan.madrassa.data.model.Ustadh;
import com.zamcan.madrassa.domain.repository.UstadhStore;

import java.util.ArrayList;
import java.util.List;

public class UstadhRepository implements UstadhStore {

    private final EduNoorDatabase database;

    public UstadhRepository(Context context) {
        database = new EduNoorDatabase(context);
    }

    @Override
    public Ustadh findById(String ustadhId) {

        if (isBlank(ustadhId)) {
            return null;
        }

        SQLiteDatabase db =
                database.getReadableDatabase();

        Cursor cursor = db.query(
                "ustadhs",
                null,
                "id = ?",
                new String[]{ustadhId},
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

    @Override
    public List<Ustadh> findByMadrassa(
            String madrassaId
    ) {

        return findByMadrassaInternal(
                madrassaId,
                false
        );
    }

    @Override
    public List<Ustadh> findActiveByMadrassa(
            String madrassaId
    ) {

        return findByMadrassaInternal(
                madrassaId,
                true
        );
    }

    @Override
    public Ustadh findActiveHeadByMadrassa(
            String madrassaId
    ) {
        if (madrassaId == null ||
                madrassaId.trim().isEmpty()) {
            return null;
        }

        SQLiteDatabase db =
                database.getReadableDatabase();

        Cursor cursor = db.query(
                "ustadhs",
                null,
                "madrassa_id = ? AND head_ustadh = 1 AND active = 1",
                new String[]{
                        madrassaId.trim()
                },
                null,
                null,
                "id ASC",
                "2"
        );

        try {
            if (!cursor.moveToFirst()) {
                return null;
            }

            Ustadh first = fromCursor(cursor);

            if (cursor.moveToNext()) {
                throw new IllegalStateException(
                        "Multiple active Head Ustadh accounts found."
                );
            }

            return first;

        } finally {
            cursor.close();
        }
    }

    @Override
    public Ustadh findByPhone(String phone) {

        if (isBlank(phone)) {
            return null;
        }

        SQLiteDatabase db =
                database.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM ustadhs " +
                "WHERE phone = ? " +
                "LIMIT 1",
                new String[]{
                        normalizePhone(phone)
                }
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

    @Override
    public boolean existsByPhone(String phone) {

        if (isBlank(phone)) {
            return false;
        }

        SQLiteDatabase db =
                database.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT 1 FROM ustadhs " +
                "WHERE phone = ? " +
                "LIMIT 1",
                new String[]{
                        normalizePhone(phone)
                }
        );

        try {
            return cursor.moveToFirst();
        } finally {
            cursor.close();
        }
    }

    @Override
    public void save(Ustadh ustadh) {

        if (ustadh == null ||
                isBlank(ustadh.id) ||
                isBlank(ustadh.madrassaId) ||
                isBlank(ustadh.fullName)) {
            return;
        }

        SQLiteDatabase db =
                database.getWritableDatabase();

        ContentValues values =
                toValues(ustadh);

        db.insertWithOnConflict(
                "ustadhs",
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
        );
    }

    @Override
    public void update(Ustadh ustadh) {

        if (ustadh == null ||
                isBlank(ustadh.id)) {
            return;
        }

        SQLiteDatabase db =
                database.getWritableDatabase();

        db.update(
                "ustadhs",
                toValues(ustadh),
                "id = ?",
                new String[]{ustadh.id}
        );
    }

    private List<Ustadh> findByMadrassaInternal(
            String madrassaId,
            boolean activeOnly
    ) {

        List<Ustadh> result =
                new ArrayList<>();

        if (isBlank(madrassaId)) {
            return result;
        }

        SQLiteDatabase db =
                database.getReadableDatabase();

        String selection =
                activeOnly
                        ? "madrassa_id = ? AND active = 1"
                        : "madrassa_id = ?";

        Cursor cursor = db.query(
                "ustadhs",
                null,
                selection,
                new String[]{madrassaId},
                null,
                null,
                "full_name COLLATE NOCASE ASC"
        );

        try {
            while (cursor.moveToNext()) {
                result.add(
                        fromCursor(cursor)
                );
            }

            return result;

        } finally {
            cursor.close();
        }
    }

    private ContentValues toValues(
            Ustadh ustadh
    ) {

        ContentValues values =
                new ContentValues();

        values.put("id", ustadh.id);
        values.put(
                "madrassa_id",
                ustadh.madrassaId
        );
        values.put(
                "full_name",
                ustadh.fullName
        );

        values.put(
                "phone",
                normalizePhone(
                        ustadh.phone
                )
        );

        values.put(
                "email",
                ustadh.email
        );

        values.put(
                "head_ustadh",
                ustadh.headUstadh ? 1 : 0
        );

        values.put(
                "active",
                ustadh.active ? 1 : 0
        );

        return values;
    }

    private Ustadh fromCursor(
            Cursor cursor
    ) {

        Ustadh ustadh =
                new Ustadh();

        ustadh.id =
                getString(
                        cursor,
                        "id"
                );

        ustadh.madrassaId =
                getString(
                        cursor,
                        "madrassa_id"
                );

        ustadh.fullName =
                getString(
                        cursor,
                        "full_name"
                );

        ustadh.phone =
                getString(
                        cursor,
                        "phone"
                );

        ustadh.email =
                getString(
                        cursor,
                        "email"
                );

        ustadh.headUstadh =
                getInt(
                        cursor,
                        "head_ustadh"
                ) == 1;

        ustadh.active =
                getInt(
                        cursor,
                        "active"
                ) == 1;

        return ustadh;
    }

    private String normalizePhone(
            String phone
    ) {

        if (phone == null) {
            return null;
        }

        String value =
                phone.trim()
                        .replaceAll(
                                "[\\s\\-().]",
                                ""
                        );

        if (value.startsWith("00")) {
            value =
                    "+" + value.substring(2);
        }

        if (value.startsWith("+")) {
            return "+" +
                    value.substring(1)
                            .replaceAll(
                                    "[^0-9]",
                                    ""
                            );
        }

        return value.replaceAll(
                "[^0-9]",
                ""
        );
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

    private int getInt(
            Cursor cursor,
            String column
    ) {

        int index =
                cursor.getColumnIndexOrThrow(
                        column
                );

        return cursor.getInt(index);
    }

    private boolean isBlank(
            String value
    ) {

        return value == null ||
                value.trim().isEmpty();
    }

    public void close() {
        database.close();
    }
}
