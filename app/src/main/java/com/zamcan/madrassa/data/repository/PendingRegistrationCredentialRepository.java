package com.zamcan.madrassa.data.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.zamcan.madrassa.data.local.EduNoorDatabase;
import com.zamcan.madrassa.data.model.AccountRole;
import com.zamcan.madrassa.data.model.PendingRegistrationCredential;
import com.zamcan.madrassa.domain.repository.PendingRegistrationCredentialStore;

public final class PendingRegistrationCredentialRepository
        implements PendingRegistrationCredentialStore {

    private final EduNoorDatabase database;

    public PendingRegistrationCredentialRepository(
            Context context
    ) {
        if (context == null) {
            throw new IllegalArgumentException(
                    "context must not be null"
            );
        }

        database =
                new EduNoorDatabase(
                        context.getApplicationContext()
                );
    }

    @Override
    public PendingRegistrationCredential
    findByMadrassaAndRole(
            String madrassaId,
            AccountRole role
    ) {
        if (isBlank(madrassaId) ||
                role == null) {
            return null;
        }

        SQLiteDatabase db =
                database.getReadableDatabase();

        Cursor cursor = db.query(
                "pending_registration_credentials",
                null,
                "madrassa_id = ? AND role = ?",
                new String[]{
                        madrassaId.trim(),
                        role.name()
                },
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

    @Override
    public boolean existsByMadrassaAndRole(
            String madrassaId,
            AccountRole role
    ) {
        return findByMadrassaAndRole(
                madrassaId,
                role
        ) != null;
    }

    @Override
    public void save(
            PendingRegistrationCredential credential
    ) {
        validate(credential);

        SQLiteDatabase db =
                database.getWritableDatabase();

        db.insertOrThrow(
                "pending_registration_credentials",
                null,
                toValues(credential)
        );
    }

    @Override
    public void update(
            PendingRegistrationCredential credential
    ) {
        validate(credential);

        SQLiteDatabase db =
                database.getWritableDatabase();

        int updated =
                db.update(
                        "pending_registration_credentials",
                        toValues(credential),
                        "id = ?",
                        new String[]{
                                credential.id
                        }
                );

        if (updated != 1) {
            throw new IllegalStateException(
                    "Pending credential not found."
            );
        }
    }

    @Override
    public void delete(
            String madrassaId,
            AccountRole role
    ) {
        if (isBlank(madrassaId) ||
                role == null) {
            return;
        }

        SQLiteDatabase db =
                database.getWritableDatabase();

        db.delete(
                "pending_registration_credentials",
                "madrassa_id = ? AND role = ?",
                new String[]{
                        madrassaId.trim(),
                        role.name()
                }
        );
    }

    private PendingRegistrationCredential fromCursor(
            Cursor cursor
    ) {
        PendingRegistrationCredential credential =
                new PendingRegistrationCredential();

        credential.id =
                cursor.getString(
                        cursor.getColumnIndexOrThrow("id")
                );

        credential.madrassaId =
                cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                "madrassa_id"
                        )
                );

        String role =
                cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                "role"
                        )
                );

        credential.role =
                AccountRole.valueOf(role);

        credential.passwordHash =
                cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                "password_hash"
                        )
                );

        credential.passwordSalt =
                cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                "password_salt"
                        )
                );

        credential.createdAt =
                cursor.getLong(
                        cursor.getColumnIndexOrThrow(
                                "created_at"
                        )
                );

        credential.updatedAt =
                cursor.getLong(
                        cursor.getColumnIndexOrThrow(
                                "updated_at"
                        )
                );

        return credential;
    }

    private ContentValues toValues(
            PendingRegistrationCredential credential
    ) {
        ContentValues values =
                new ContentValues();

        values.put(
                "id",
                credential.id
        );

        values.put(
                "madrassa_id",
                credential.madrassaId
        );

        values.put(
                "role",
                credential.role.name()
        );

        values.put(
                "password_hash",
                credential.passwordHash
        );

        values.put(
                "password_salt",
                credential.passwordSalt
        );

        values.put(
                "created_at",
                credential.createdAt
        );

        values.put(
                "updated_at",
                credential.updatedAt
        );

        return values;
    }

    private void validate(
            PendingRegistrationCredential credential
    ) {
        if (credential == null) {
            throw new IllegalArgumentException(
                    "credential must not be null"
            );
        }

        if (isBlank(credential.id)) {
            throw new IllegalArgumentException(
                    "credential.id is required"
            );
        }

        if (isBlank(credential.madrassaId)) {
            throw new IllegalArgumentException(
                    "credential.madrassaId is required"
            );
        }

        if (credential.role == null) {
            throw new IllegalArgumentException(
                    "credential.role is required"
            );
        }

        if (isBlank(credential.passwordHash)) {
            throw new IllegalArgumentException(
                    "credential.passwordHash is required"
            );
        }

        if (isBlank(credential.passwordSalt)) {
            throw new IllegalArgumentException(
                    "credential.passwordSalt is required"
            );
        }

        if (credential.createdAt <= 0) {
            throw new IllegalArgumentException(
                    "credential.createdAt is required"
            );
        }

        if (credential.updatedAt <= 0) {
            throw new IllegalArgumentException(
                    "credential.updatedAt is required"
            );
        }
    }

    private static boolean isBlank(
            String value
    ) {
        return value == null ||
                value.trim().isEmpty();
    }
}
