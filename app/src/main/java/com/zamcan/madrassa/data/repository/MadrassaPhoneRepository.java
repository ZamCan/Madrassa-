package com.zamcan.madrassa.data.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.zamcan.madrassa.data.local.EduNoorDatabase;
import com.zamcan.madrassa.data.model.MadrassaPhone;

import java.util.ArrayList;
import java.util.List;

public class MadrassaPhoneRepository {

    private final EduNoorDatabase database;

    public MadrassaPhoneRepository(Context context) {
        database =
                new EduNoorDatabase(context);
    }

    public boolean save(
            MadrassaPhone phone
    ) {

        if (phone == null ||
                isBlank(phone.id) ||
                isBlank(phone.madrassaId) ||
                isBlank(phone.phone)) {
            return false;
        }

        SQLiteDatabase db =
                database.getWritableDatabase();

        ContentValues values =
                new ContentValues();

        values.put(
                "id",
                phone.id
        );

        values.put(
                "madrassa_id",
                phone.madrassaId
        );

        values.put(
                "phone",
                normalizePhone(
                        phone.phone
                )
        );

        values.put(
                "label",
                phone.label
        );

        values.put(
                "primary_phone",
                phone.primary ? 1 : 0
        );

        values.put(
                "active",
                phone.active ? 1 : 0
        );

        values.put(
                "created_at",
                phone.createdAt
        );

        values.put(
                "verified_at",
                phone.verifiedAt
        );

        long result =
                db.insertWithOnConflict(
                        "madrassa_phones",
                        null,
                        values,
                        SQLiteDatabase.CONFLICT_REPLACE
                );

        return result != -1;
    }

    public List<MadrassaPhone>
    findByMadrassa(
            String madrassaId
    ) {

        List<MadrassaPhone> result =
                new ArrayList<>();

        if (isBlank(madrassaId)) {
            return result;
        }

        SQLiteDatabase db =
                database.getReadableDatabase();

        Cursor cursor = db.query(
                "madrassa_phones",
                null,
                "madrassa_id = ? AND active = 1",
                new String[]{madrassaId},
                null,
                null,
                "primary_phone DESC, phone ASC"
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

    public MadrassaPhone findByPhone(
            String phone
    ) {

        if (isBlank(phone)) {
            return null;
        }

        SQLiteDatabase db =
                database.getReadableDatabase();

        Cursor cursor = db.query(
                "madrassa_phones",
                null,
                "phone = ? AND active = 1",
                new String[]{
                        normalizePhone(phone)
                },
                null,
                null,
                null,
                "1"
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

    private MadrassaPhone fromCursor(
            Cursor cursor
    ) {

        MadrassaPhone phone =
                new MadrassaPhone();

        phone.id =
                getString(
                        cursor,
                        "id"
                );

        phone.madrassaId =
                getString(
                        cursor,
                        "madrassa_id"
                );

        phone.phone =
                getString(
                        cursor,
                        "phone"
                );

        phone.label =
                getString(
                        cursor,
                        "label"
                );

        phone.primary =
                getInt(
                        cursor,
                        "primary_phone"
                ) == 1;

        phone.active =
                getInt(
                        cursor,
                        "active"
                ) == 1;

        phone.createdAt =
                getLong(
                        cursor,
                        "created_at"
                );

        phone.verifiedAt =
                getLong(
                        cursor,
                        "verified_at"
                );

        return phone;
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

        return cursor.getInt(
                cursor.getColumnIndexOrThrow(
                        column
                )
        );
    }

    private long getLong(
            Cursor cursor,
            String column
    ) {

        return cursor.getLong(
                cursor.getColumnIndexOrThrow(
                        column
                )
        );
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
