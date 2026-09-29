package com.zamcan.madrassa.data.repository;

import android.content.ContentValues;
import android.database.Cursor;
import com.zamcan.madrassa.data.local.EduNoorDatabase;
import com.zamcan.madrassa.data.model.Attendance;
import com.zamcan.madrassa.data.model.AttendanceStatus;
import com.zamcan.madrassa.domain.repository.AttendanceStore;
import java.util.ArrayList;
import java.util.List;

public final class AttendanceRepository implements AttendanceStore {
    private final EduNoorDatabase database;

    public AttendanceRepository(EduNoorDatabase database) {
        if (database == null) throw new IllegalArgumentException("database is required");
        this.database = database;
    }

    @Override public Attendance findById(String id) {
        if (blank(id)) return null;
        Cursor c = database.getReadableDatabase().query(
                "attendance", null, "id = ?", new String[]{id.trim()},
                null, null, null, "1");
        try { return c.moveToFirst() ? map(c) : null; }
        finally { c.close(); }
    }

    @Override public List<Attendance> findByStudent(String studentId) {
        return find("student_id = ?", new String[]{studentId}, "date DESC, id DESC");
    }

    @Override public List<Attendance> findByClassAndDate(String classId, String date) {
        if (blank(classId) || blank(date)) return new ArrayList<>();
        return find("class_id = ? AND date = ?",
                new String[]{classId.trim(), date.trim()}, "student_id ASC");
    }

    @Override public boolean save(Attendance x) {
        validate(x);
        return database.getWritableDatabase()
                .insert("attendance", null, values(x)) != -1;
    }

    @Override public boolean update(Attendance x) {
        validate(x);
        return database.getWritableDatabase()
                .update("attendance", values(x), "id = ?",
                        new String[]{x.id.trim()}) == 1;
    }

    private List<Attendance> find(String where, String[] args, String order) {
        List<Attendance> out = new ArrayList<>();
        for (String arg : args) if (blank(arg)) return out;
        Cursor c = database.getReadableDatabase().query(
                "attendance", null, where, args, null, null, order);
        try { while (c.moveToNext()) out.add(map(c)); }
        finally { c.close(); }
        return out;
    }

    private Attendance map(Cursor c) {
        Attendance x = new Attendance();
        x.id = c.getString(c.getColumnIndexOrThrow("id"));
        x.studentId = c.getString(c.getColumnIndexOrThrow("student_id"));
        x.classId = c.getString(c.getColumnIndexOrThrow("class_id"));
        x.date = c.getString(c.getColumnIndexOrThrow("date"));
        x.status = parse(c.getString(c.getColumnIndexOrThrow("status")));
        x.note = text(c, "note");
        x.recordedBy = text(c, "recorded_by");
        return x;
    }

    private ContentValues values(Attendance x) {
        ContentValues v = new ContentValues();
        v.put("id", x.id.trim());
        v.put("student_id", x.studentId.trim());
        v.put("class_id", x.classId.trim());
        v.put("date", x.date.trim());
        v.put("status", x.status.name());
        if (x.note == null) v.putNull("note"); else v.put("note", x.note.trim());
        if (x.recordedBy == null) v.putNull("recorded_by"); else v.put("recorded_by", x.recordedBy.trim());
        return v;
    }

    private static AttendanceStatus parse(String value) {
        try { return AttendanceStatus.valueOf(value); }
        catch (Exception e) { return AttendanceStatus.ABSENT; }
    }

    private static String text(Cursor c, String column) {
        int i = c.getColumnIndex(column);
        return i < 0 || c.isNull(i) ? null : c.getString(i);
    }

    private static void validate(Attendance x) {
        if (x == null || blank(x.id) || blank(x.studentId) || blank(x.classId)
                || blank(x.date) || x.status == null) {
            throw new IllegalArgumentException("attendance data is incomplete");
        }
    }

    private static boolean blank(String v) { return v == null || v.trim().isEmpty(); }
}
