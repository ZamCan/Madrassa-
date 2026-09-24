package com.zamcan.madrassa.data.repository;

import android.content.ContentValues;
import android.database.Cursor;

import com.zamcan.madrassa.data.local.EduNoorDatabase;
import com.zamcan.madrassa.data.model.Fee;
import com.zamcan.madrassa.data.model.FeeStatus;
import com.zamcan.madrassa.domain.repository.FeeStore;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite fee persistence used by authorized Madrassa views.
 *
 * <p>Dashboard reads are tenant-filtered by the caller. Writes satisfy
 * the domain store contract and validate every field before touching
 * the database.</p>
 */
public final class FeeRepository implements FeeStore {

    private final EduNoorDatabase database;

    public FeeRepository(EduNoorDatabase database) {
        if (database == null) {
            throw new IllegalArgumentException("database is required");
        }
        this.database = database;
    }

    @Override
    public Fee findById(String id) {
        if (blank(id)) {
            return null;
        }

        Cursor cursor = database.getReadableDatabase().query(
                "fees",
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
    public List<Fee> findByStudent(String studentId) {
        return find("student_id = ?", studentId);
    }

    @Override
    public List<Fee> findByMadrassa(String madrassaId) {
        return find("madrassa_id = ?", madrassaId);
    }

    /** Outstanding total (UNPAID + OVERDUE) of a fee list. */
    public static double outstanding(List<Fee> fees) {
        double total = 0;
        if (fees == null) {
            return total;
        }

        for (Fee fee : fees) {
            if (fee != null
                    && (fee.status == FeeStatus.UNPAID
                    || fee.status == FeeStatus.OVERDUE)) {
                total += fee.amount;
            }
        }
        return total;
    }

    private List<Fee> find(String selection, String id) {
        List<Fee> result = new ArrayList<>();

        if (blank(id)) {
            return result;
        }

        Cursor cursor = database.getReadableDatabase().query(
                "fees",
                null,
                selection,
                new String[]{id.trim()},
                null,
                null,
                "deadline ASC, submitted_at DESC"
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

    @Override
    public boolean save(Fee fee) {
        validate(fee);
        return database.getWritableDatabase().insert(
                "fees",
                null,
                values(fee)
        ) != -1;
    }

    @Override
    public boolean update(Fee fee) {
        validate(fee);
        return database.getWritableDatabase().update(
                "fees",
                values(fee),
                "id = ?",
                new String[]{fee.id.trim()}
        ) == 1;
    }

    private Fee map(Cursor cursor) {
        Fee fee = new Fee();
        fee.id = cursor.getString(cursor.getColumnIndexOrThrow("id"));
        fee.studentId = cursor.getString(
                cursor.getColumnIndexOrThrow("student_id")
        );
        fee.madrassaId = cursor.getString(
                cursor.getColumnIndexOrThrow("madrassa_id")
        );
        fee.type = cursor.getString(
                cursor.getColumnIndexOrThrow("type")
        );
        fee.amount = cursor.getDouble(
                cursor.getColumnIndexOrThrow("amount")
        );
        fee.deadline = text(cursor, "deadline");
        fee.status = parseStatus(cursor.getString(
                cursor.getColumnIndexOrThrow("status")
        ));
        fee.submittedAt = cursor.getLong(
                cursor.getColumnIndexOrThrow("submitted_at")
        );
        fee.confirmedAt = cursor.getLong(
                cursor.getColumnIndexOrThrow("confirmed_at")
        );
        fee.lockedAt = cursor.getLong(
                cursor.getColumnIndexOrThrow("locked_at")
        );
        fee.submittedBy = text(cursor, "submitted_by");
        fee.confirmedBy = text(cursor, "confirmed_by");
        return fee;
    }

    private ContentValues values(Fee fee) {
        ContentValues values = new ContentValues();
        values.put("id", fee.id.trim());
        values.put("student_id", fee.studentId.trim());
        values.put("madrassa_id", fee.madrassaId.trim());
        values.put("type", fee.type.trim());
        values.put("amount", fee.amount);
        put(values, "deadline", fee.deadline);
        values.put("status", fee.status.name());
        values.put("submitted_at", fee.submittedAt);
        values.put("confirmed_at", fee.confirmedAt);
        values.put("locked_at", fee.lockedAt);
        put(values, "submitted_by", fee.submittedBy);
        put(values, "confirmed_by", fee.confirmedBy);
        return values;
    }

    private static void put(ContentValues values, String key, String value) {
        if (value == null) {
            values.putNull(key);
        } else {
            values.put(key, value.trim());
        }
    }

    private static FeeStatus parseStatus(String value) {
        try {
            return FeeStatus.valueOf(value);
        } catch (Exception ignored) {
            return FeeStatus.UNPAID;
        }
    }

    private static String text(Cursor cursor, String column) {
        int index = cursor.getColumnIndex(column);
        return index < 0 || cursor.isNull(index)
                ? null
                : cursor.getString(index);
    }

    private static void validate(Fee fee) {
        if (fee == null
                || blank(fee.id)
                || blank(fee.studentId)
                || blank(fee.madrassaId)
                || blank(fee.type)
                || fee.status == null
                || fee.amount < 0) {
            throw new IllegalArgumentException(
                    "fee data is incomplete"
            );
        }
    }

    private static boolean blank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
