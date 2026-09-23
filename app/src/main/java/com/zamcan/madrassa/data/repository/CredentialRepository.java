package com.zamcan.madrassa.data.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.zamcan.madrassa.data.local.EduNoorDatabase;
import com.zamcan.madrassa.data.model.AccountCredential;
import com.zamcan.madrassa.data.model.AccountRole;
import com.zamcan.madrassa.domain.repository.CredentialStore;

public class CredentialRepository implements CredentialStore {

    private final EduNoorDatabase database;

    public CredentialRepository(
            EduNoorDatabase database
    ) {
        if (database == null) {
            throw new IllegalArgumentException(
                    "database must not be null"
            );
        }

        this.database = database;
    }

    @Override
    public AccountCredential findById(
            String id
    ) {
        if (isBlank(id)) {
            return null;
        }

        SQLiteDatabase db =
                database.getReadableDatabase();

        Cursor cursor = db.query(
                "credentials",
                null,
                "id = ?",
                new String[]{id.trim()},
                null,
                null,
                null,
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
    public AccountCredential findByAccount(
            String accountId,
            AccountRole role
    ) {
        if (isBlank(accountId) || role == null) {
            return null;
        }

        SQLiteDatabase db =
                database.getReadableDatabase();

        Cursor cursor = db.query(
                "credentials",
                null,
                "account_id = ? AND role = ?",
                new String[]{
                        accountId.trim(),
                        role.name()
                },
                null,
                null,
                null,
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
    public boolean existsByAccount(
            String accountId,
            AccountRole role
    ) {
        return findByAccount(
                accountId,
                role
        ) != null;
    }

    @Override
    public void save(
            AccountCredential credential
    ) {
        validateCredential(credential);

        SQLiteDatabase db =
                database.getWritableDatabase();

        ContentValues values =
                toContentValues(credential);

        db.insertOrThrow(
                "credentials",
                null,
                values
        );
    }

    @Override
    public void update(
            AccountCredential credential
    ) {
        validateCredential(credential);

        SQLiteDatabase db =
                database.getWritableDatabase();

        ContentValues values =
                toContentValues(credential);

        int updated = db.update(
                "credentials",
                values,
                "id = ?",
                new String[]{
                        credential.id.trim()
                }
        );

        if (updated == 0) {
            throw new IllegalStateException(
                    "Credential not found: " +
                    credential.id
            );
        }
    }

    private AccountCredential fromCursor(
            Cursor cursor
    ) {
        AccountCredential credential =
                new AccountCredential();

        credential.id =
                cursor.getString(
                        cursor.getColumnIndexOrThrow("id")
                );

        credential.accountId =
                cursor.getString(
                        cursor.getColumnIndexOrThrow("account_id")
                );

        String role =
                cursor.getString(
                        cursor.getColumnIndexOrThrow("role")
                );

        credential.role =
                parseRole(role);

        credential.madrassaId =
                cursor.getString(
                        cursor.getColumnIndexOrThrow("madrassa_id")
                );

        credential.passwordHash =
                cursor.getString(
                        cursor.getColumnIndexOrThrow("password_hash")
                );

        credential.passwordSalt =
                cursor.getString(
                        cursor.getColumnIndexOrThrow("password_salt")
                );

        credential.active =
                cursor.getInt(
                        cursor.getColumnIndexOrThrow("active")
                ) != 0;

        credential.firstLogin =
                cursor.getInt(
                        cursor.getColumnIndexOrThrow("first_login")
                ) != 0;

        credential.createdAt =
                cursor.getLong(
                        cursor.getColumnIndexOrThrow("created_at")
                );

        credential.updatedAt =
                cursor.getLong(
                        cursor.getColumnIndexOrThrow("updated_at")
                );

        credential.lastLoginAt =
                cursor.getLong(
                        cursor.getColumnIndexOrThrow("last_login_at")
                );

        return credential;
    }

    private ContentValues toContentValues(
            AccountCredential credential
    ) {
        ContentValues values =
                new ContentValues();

        values.put(
                "id",
                credential.id
        );

        values.put(
                "account_id",
                credential.accountId
        );

        values.put(
                "role",
                credential.role.name()
        );

        if (credential.madrassaId == null) {
            values.putNull("madrassa_id");
        } else {
            values.put(
                    "madrassa_id",
                    credential.madrassaId
            );
        }

        values.put(
                "password_hash",
                credential.passwordHash
        );

        values.put(
                "password_salt",
                credential.passwordSalt
        );

        values.put(
                "active",
                credential.active ? 1 : 0
        );

        values.put(
                "first_login",
                credential.firstLogin ? 1 : 0
        );

        values.put(
                "created_at",
                credential.createdAt
        );

        values.put(
                "updated_at",
                credential.updatedAt
        );

        values.put(
                "last_login_at",
                credential.lastLoginAt
        );

        return values;
    }

    private void validateCredential(
            AccountCredential credential
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

        if (isBlank(credential.accountId)) {
            throw new IllegalArgumentException(
                    "credential.accountId is required"
            );
        }

        if (credential.role == null) {
            throw new IllegalArgumentException(
                    "credential.role is required"
            );
        }

        if (isBlank(credential.passwordHash)) {
            throw new IllegalArgumentException(
                    "passwordHash is required"
            );
        }

        if (isBlank(credential.passwordSalt)) {
            throw new IllegalArgumentException(
                    "passwordSalt is required"
            );
        }
    }

    private AccountRole parseRole(
            String value
    ) {
        if (isBlank(value)) {
            throw new IllegalStateException(
                    "Credential role is missing."
            );
        }

        try {
            return AccountRole.valueOf(
                    value
            );
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(
                    "Unknown credential role: " +
                    value,
                    exception
            );
        }
    }

    private boolean isBlank(
            String value
    ) {
        return value == null ||
                value.trim().isEmpty();
    }
}
