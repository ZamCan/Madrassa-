package com.zamcan.madrassa.data.repository;

import android.content.ContentValues;
import android.database.Cursor;
import com.zamcan.madrassa.data.local.EduNoorDatabase;
import com.zamcan.madrassa.data.model.Fee;
import com.zamcan.madrassa.data.model.FeeStatus;
import com.zamcan.madrassa.domain.repository.FeeStore;
import java.util.ArrayList;
import java.util.List;

public final class FeeRepository implements FeeStore {
    private final EduNoorDatabase database;

    public FeeRepository(EduNoorDatabase database) {
        if (database == null) throw new IllegalArgumentException("database is required");
        this.database = database;
    }

    @Override public Fee findById(String id) {
        if (blank(id)) return null;
        Cursor c = database.getReadableDatabase().query(
                "fees", null, "id = ?", new String[]{id.trim()},
                null, null, null, "1");
        try { return c.moveToFirst() ? map(c) : null; }
        finally { c.close(); }
    }

    @Override public List<Fee> findByStudent(String studentId) {
        return find("student_id = ?", studentId, "deadline ASC, id ASC");
    }

    @Override public List<Fee> findByMadrassa(String madrassaId) {
        return find("madrassa_id = ?", madrassaId, "deadline ASC, id ASC");
    }

    @Override public boolean save(Fee fee) {
        validate(fee);
        return database.getWritableDatabase()
                .insert("fees", null, values(fee)) != -1;
    }

    @Override public boolean update(Fee fee) {
        validate(fee);
        return database.getWritableDatabase()
                .update("fees", values(fee), "id = ?",
                        new String[]{fee.id.trim()}) == 1;
    }

    private List<Fee> find(String where, String id, String order) {
        List<Fee> out = new ArrayList<>();
        if (blank(id)) return out;
        Cursor c = database.getReadableDatabase().query(
                "fees", null, where, new String[]{id.trim()},
                null, null, order);
        try { while (c.moveToNext()) out.add(map(c)); }
        finally { c.close(); }
        return out;
    }

    private Fee map(Cursor c) {
        Fee x = new Fee();
        x.id = c.getString(c.getColumnIndexOrThrow("id"));
        x.studentId = c.getString(c.getColumnIndexOrThrow("student_id"));
        x.madrassaId = c.getString(c.getColumnIndexOrThrow("madrassa_id"));
        x.type = c.getString(c.getColumnIndexOrThrow("type"));
        x.amount = c.getDouble(c.getColumnIndexOrThrow("amount"));
        x.deadline = text(c, "deadline");
        x.status = parse(c.getString(c.getColumnIndexOrThrow("status")));
        x.submittedAt = c.getLong(c.getColumnIndexOrThrow("submitted_at"));
        x.confirmedAt = c.getLong(c.getColumnIndexOrThrow("confirmed_at"));
        x.lockedAt = c.getLong(c.getColumnIndexOrThrow("locked_at"));
        x.submittedBy = text(c, "submitted_by");
        x.confirmedBy = text(c, "confirmed_by");
        return x;
    }

    private ContentValues values(Fee x) {
        ContentValues v = new ContentValues();
        v.put("id", x.id.trim());
        v.put("student_id", x.studentId.trim());
        v.put("madrassa_id", x.madrassaId.trim());
        v.put("type", x.type.trim());
        v.put("amount", x.amount);
        if (x.deadline == null) v.putNull("deadline"); else v.put("deadline", x.deadline.trim());
        v.put("status", x.status.name());
        v.put("submitted_at", x.submittedAt);
        v.put("confirmed_at", x.confirmedAt);
        v.put("locked_at", x.lockedAt);
        if (x.submittedBy == null) v.putNull("submitted_by"); else v.put("submitted_by", x.submittedBy.trim());
        if (x.confirmedBy == null) v.putNull("confirmed_by"); else v.put("confirmed_by", x.confirmedBy.trim());
        return v;
    }

    private static FeeStatus parse(String value) {
        try { return FeeStatus.valueOf(value); }
        catch (Exception e) { return FeeStatus.UNPAID; }
    }

    private static String text(Cursor c, String column) {
        int i = c.getColumnIndex(column);
        return i < 0 || c.isNull(i) ? null : c.getString(i);
    }

    private static void validate(Fee x) {
        if (x == null || blank(x.id) || blank(x.studentId) || blank(x.madrassaId)
                || blank(x.type) || x.amount < 0 || x.status == null) {
            throw new IllegalArgumentException("fee data is incomplete");
        }
    }

    private static boolean blank(String v) { return v == null || v.trim().isEmpty(); }
}
