package com.zamcan.madrassa.data.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.zamcan.madrassa.data.local.DatabaseTransactionRunner;
import com.zamcan.madrassa.data.local.EduNoorDatabase;
import com.zamcan.madrassa.data.model.AccountCredential;
import com.zamcan.madrassa.data.model.Madrassa;
import com.zamcan.madrassa.data.model.Ustadh;
import com.zamcan.madrassa.domain.repository.HeadUstadhProvisioningStore;

public final class HeadUstadhProvisioningRepository
        implements HeadUstadhProvisioningStore {

    private final EduNoorDatabase database;
    private final DatabaseTransactionRunner transactions;

    public HeadUstadhProvisioningRepository(
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

        transactions =
                new DatabaseTransactionRunner(
                        database
                );
    }

    @Override
    public void provision(
            Madrassa madrassa,
            Ustadh ustadh,
            AccountCredential credential
    ) {
        validateInputs(
                madrassa,
                ustadh,
                credential
        );

        transactions.run(
                db -> {
                    insertUstadh(db, ustadh);
                    insertCredential(db, credential);
                    updateMadrassaHead(db, madrassa);
                }
        );
    }

    private void validateInputs(
            Madrassa madrassa,
            Ustadh ustadh,
            AccountCredential credential
    ) {
        if (madrassa == null ||
                isBlank(madrassa.id)) {
            throw new IllegalArgumentException(
                    "Madrassa is required."
            );
        }

        if (ustadh == null ||
                isBlank(ustadh.id)) {
            throw new IllegalArgumentException(
                    "Ustadh is required."
            );
        }

        if (!madrassa.id.equals(
                ustadh.madrassaId
        )) {
            throw new IllegalArgumentException(
                    "Ustadh does not belong to Madrassa."
            );
        }

        if (!ustadh.headUstadh) {
            throw new IllegalArgumentException(
                    "Provisioned Ustadh must be Head Ustadh."
            );
        }

        if (credential == null ||
                isBlank(credential.id)) {
            throw new IllegalArgumentException(
                    "Credential is required."
            );
        }

        if (!ustadh.id.equals(
                credential.accountId
        )) {
            throw new IllegalArgumentException(
                    "Credential account does not match Ustadh."
            );
        }

        if (credential.madrassaId == null ||
                !madrassa.id.equals(
                        credential.madrassaId
                )) {
            throw new IllegalArgumentException(
                    "Credential Madrassa does not match."
            );
        }
    }

    private void insertUstadh(
            SQLiteDatabase db,
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
                ustadh.phone
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

        db.insertOrThrow(
                "ustadhs",
                null,
                values
        );
    }

    private void insertCredential(
            SQLiteDatabase db,
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
        values.put(
                "madrassa_id",
                credential.madrassaId
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

        db.insertOrThrow(
                "credentials",
                null,
                values
        );
    }

    private void updateMadrassaHead(
            SQLiteDatabase db,
            Madrassa madrassa
    ) {
        ContentValues values =
                new ContentValues();

        values.put(
                "head_ustadh_id",
                madrassa.headUstadhId
        );

        int updated =
                db.update(
                        "madrassas",
                        values,
                        "id = ?",
                        new String[]{
                                madrassa.id
                        }
                );

        if (updated != 1) {
            throw new IllegalStateException(
                    "Madrassa provisioning update failed."
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
