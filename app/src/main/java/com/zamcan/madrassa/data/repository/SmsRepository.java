package com.zamcan.madrassa.data.repository;

import android.content.ContentValues;
import android.database.Cursor;
import com.zamcan.madrassa.data.local.EduNoorDatabase;
import com.zamcan.madrassa.data.model.SmsMessage;
import com.zamcan.madrassa.data.model.SmsStatus;
import com.zamcan.madrassa.domain.repository.SmsStore;
import java.util.ArrayList;
import java.util.List;

public final class SmsRepository implements SmsStore {
    private final EduNoorDatabase database;

    public SmsRepository(EduNoorDatabase database) {
        if (database == null) throw new IllegalArgumentException("database is required");
        this.database = database;
    }

    @Override public SmsMessage findById(String id) {
        if (blank(id)) return null;
        Cursor c = database.getReadableDatabase().query(
                "sms_messages", null, "id = ?", new String[]{id.trim()},
                null, null, null, "1");
        try { return c.moveToFirst() ? map(c) : null; }
        finally { c.close(); }
    }

    @Override public List<SmsMessage> findByMadrassa(String madrassaId) {
        List<SmsMessage> out = new ArrayList<>();
        if (blank(madrassaId)) return out;
        Cursor c = database.getReadableDatabase().query(
                "sms_messages", null, "madrassa_id = ?",
                new String[]{madrassaId.trim()}, null, null,
                "created_at DESC");
        try { while (c.moveToNext()) out.add(map(c)); }
        finally { c.close(); }
        return out;
    }

    @Override public boolean save(SmsMessage x) {
        validate(x);
        return database.getWritableDatabase()
                .insert("sms_messages", null, values(x)) != -1;
    }

    @Override public boolean update(SmsMessage x) {
        validate(x);
        return database.getWritableDatabase()
                .update("sms_messages", values(x), "id = ?",
                        new String[]{x.id.trim()}) == 1;
    }

    private SmsMessage map(Cursor c) {
        SmsMessage x = new SmsMessage();
        x.id = c.getString(c.getColumnIndexOrThrow("id"));
        x.madrassaId = c.getString(c.getColumnIndexOrThrow("madrassa_id"));
        x.recipient = c.getString(c.getColumnIndexOrThrow("recipient"));
        x.message = c.getString(c.getColumnIndexOrThrow("message"));
        x.type = c.getString(c.getColumnIndexOrThrow("type"));
        x.provider = text(c, "provider");
        x.status = parse(c.getString(c.getColumnIndexOrThrow("status")));
        x.createdAt = c.getLong(c.getColumnIndexOrThrow("created_at"));
        x.sentAt = c.getLong(c.getColumnIndexOrThrow("sent_at"));
        x.deliveredAt = c.getLong(c.getColumnIndexOrThrow("delivered_at"));
        return x;
    }

    private ContentValues values(SmsMessage x) {
        ContentValues v = new ContentValues();
        v.put("id", x.id.trim());
        v.put("madrassa_id", x.madrassaId.trim());
        v.put("recipient", x.recipient.trim());
        v.put("message", x.message);
        v.put("type", x.type.trim());
        if (x.provider == null) v.putNull("provider"); else v.put("provider", x.provider.trim());
        v.put("status", x.status.name());
        v.put("created_at", x.createdAt);
        v.put("sent_at", x.sentAt);
        v.put("delivered_at", x.deliveredAt);
        return v;
    }

    private static SmsStatus parse(String value) {
        try { return SmsStatus.valueOf(value); }
        catch (Exception e) { return SmsStatus.FAILED; }
    }

    private static String text(Cursor c, String column) {
        int i = c.getColumnIndex(column);
        return i < 0 || c.isNull(i) ? null : c.getString(i);
    }

    private static void validate(SmsMessage x) {
        if (x == null || blank(x.id) || blank(x.madrassaId)
                || blank(x.recipient) || blank(x.message)
                || blank(x.type) || x.status == null) {
            throw new IllegalArgumentException("sms message data is incomplete");
        }
    }

    private static boolean blank(String v) { return v == null || v.trim().isEmpty(); }
}
