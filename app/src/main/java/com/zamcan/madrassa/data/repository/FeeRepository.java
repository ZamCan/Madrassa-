package com.zamcan.madrassa.data.repository;

import com.zamcan.madrassa.data.local.EduNoorDatabase;
import com.zamcan.madrassa.data.model.Fee;
import com.zamcan.madrassa.data.model.FeeStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * Read-only access to the fees ledger (schema v10). Written for
 * the dashboards: totals and lists only - creation and approval
 * stay with the registration/management transactions.
 */
public final class FeeRepository {

    private final EduNoorDatabase database;

    public FeeRepository(EduNoorDatabase database) {
        if (database == null) {
            throw new IllegalArgumentException(
                    "database is required");
        }
        this.database = database;
    }

    public List<Fee> findByStudent(String studentId) {
        return find("student_id = ?", studentId);
    }

    public List<Fee> findByMadrassa(String madrassaId) {
        return find("madrassa_id = ?", madrassaId);
    }

    /** Inserts one fee record (used by the dev seed). */
    public boolean save(Fee fee) {

        if (fee == null
                || fee.id == null || fee.id.trim().isEmpty()
                || fee.studentId == null
                || fee.madrassaId == null) {
            return false;
        }

        android.content.ContentValues values =
                new android.content.ContentValues();
        values.put("id", fee.id.trim());
        values.put("student_id", fee.studentId.trim());
        values.put("madrassa_id", fee.madrassaId.trim());
        values.put("type", fee.type == null ? "" : fee.type);
        values.put("amount", fee.amount);
        values.put("deadline", fee.deadline);
        values.put("status", fee.status == null
                ? FeeStatus.UNPAID.name() : fee.status.name());
        values.put("submitted_at", fee.submittedAt);
        values.put("confirmed_at", fee.confirmedAt);
        values.put("locked_at", fee.lockedAt);
        values.put("submitted_by", fee.submittedBy);
        values.put("confirmed_by", fee.confirmedBy);

        return database.getWritableDatabase()
                .insert("fees", null, values) != -1;
    }

    private List<Fee> find(String where, String value) {

        List<Fee> result = new ArrayList<>();

        if (value == null || value.trim().isEmpty()) {
            return result;
        }

        android.database.Cursor cursor = database
                .getReadableDatabase()
                .query(
                        "fees",
                        null,
                        where,
                        new String[]{value.trim()},
                        null,
                        null,
                        "deadline ASC"
                );

        try {
            while (cursor.moveToNext()) {
                Fee fee = new Fee();
                fee.id = cursor.getString(
                        cursor.getColumnIndexOrThrow("id"));
                fee.studentId = cursor.getString(
                        cursor.getColumnIndexOrThrow("student_id"));
                fee.madrassaId = cursor.getString(
                        cursor.getColumnIndexOrThrow("madrassa_id"));
                fee.type = cursor.getString(
                        cursor.getColumnIndexOrThrow("type"));
                fee.amount = cursor.getDouble(
                        cursor.getColumnIndexOrThrow("amount"));
                fee.deadline = cursor.getString(
                        cursor.getColumnIndexOrThrow("deadline"));

                String status = cursor.getString(
                        cursor.getColumnIndexOrThrow("status"));
                try {
                    fee.status = FeeStatus.valueOf(status);
                } catch (Exception error) {
                    fee.status = FeeStatus.UNPAID;
                }

                fee.submittedAt = cursor.getLong(
                        cursor.getColumnIndexOrThrow("submitted_at"));
                fee.confirmedAt = cursor.getLong(
                        cursor.getColumnIndexOrThrow("confirmed_at"));
                fee.lockedAt = cursor.getLong(
                        cursor.getColumnIndexOrThrow("locked_at"));
                fee.submittedBy = cursor.getString(
                        cursor.getColumnIndexOrThrow("submitted_by"));
                fee.confirmedBy = cursor.getString(
                        cursor.getColumnIndexOrThrow("confirmed_by"));

                result.add(fee);
            }
        } finally {
            cursor.close();
        }

        return result;
    }

    /** Outstanding total (UNPAID + OVERDUE) of a fee list. */
    public static double outstanding(List<Fee> fees) {
        double total = 0;
        for (Fee fee : fees) {
            if (fee.status == FeeStatus.UNPAID
                    || fee.status == FeeStatus.OVERDUE) {
                total += fee.amount;
            }
        }
        return total;
    }
}
