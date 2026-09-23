package com.zamcan.madrassa.data.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.zamcan.madrassa.data.local.EduNoorDatabase;
import com.zamcan.madrassa.data.model.AccountCredential;
import com.zamcan.madrassa.data.model.AccountRole;
import com.zamcan.madrassa.data.model.Parent;
import com.zamcan.madrassa.data.local.DatabaseTransactionRunner;
import com.zamcan.madrassa.domain.repository.ParentProvisioningStore;

public final class ParentProvisioningRepository
        implements ParentProvisioningStore {

    private final EduNoorDatabase database;
    private final DatabaseTransactionRunner transactionRunner;

    public ParentProvisioningRepository(Context context) {
        if (context == null) {
            throw new IllegalArgumentException(
                    "context is required"
            );
        }

        this.database =
                new EduNoorDatabase(
                        context.getApplicationContext()
                );

        this.transactionRunner =
                new DatabaseTransactionRunner(database);
    }

    @Override
    public void provision(
            Parent parent,
            AccountCredential credential
    ) {
        validate(parent, credential);

        transactionRunner.run(db -> {

            ContentValues parentValues = new ContentValues();

            parentValues.put("id", parent.id);
            parentValues.put("madrassa_id", parent.madrassaId);
            parentValues.put("full_name", parent.fullName);
            parentValues.put("phone", parent.phone);
            parentValues.put(
                    "first_login",
                    parent.firstLogin ? 1 : 0
            );
            parentValues.put(
                    "active",
                    parent.active ? 1 : 0
            );

            db.insertOrThrow(
                    "parents",
                    null,
                    parentValues
            );

            ContentValues credentialValues =
                    new ContentValues();

            credentialValues.put(
                    "id",
                    credential.id
            );
            credentialValues.put(
                    "account_id",
                    credential.accountId
            );
            credentialValues.put(
                    "role",
                    credential.role.name()
            );
            credentialValues.put(
                    "madrassa_id",
                    credential.madrassaId
            );
            credentialValues.put(
                    "password_hash",
                    credential.passwordHash
            );
            credentialValues.put(
                    "password_salt",
                    credential.passwordSalt
            );
            credentialValues.put(
                    "active",
                    credential.active ? 1 : 0
            );
            credentialValues.put(
                    "first_login",
                    credential.firstLogin ? 1 : 0
            );
            credentialValues.put(
                    "created_at",
                    credential.createdAt
            );
            credentialValues.put(
                    "updated_at",
                    credential.updatedAt
            );
            credentialValues.put(
                    "last_login_at",
                    credential.lastLoginAt
            );

            db.insertOrThrow(
                    "credentials",
                    null,
                    credentialValues
            );
        });
    }

    private void validate(
            Parent parent,
            AccountCredential credential
    ) {
        if (parent == null
                || credential == null) {
            throw new IllegalArgumentException(
                    "Parent and credential are required."
            );
        }

        require(parent.id, "parent.id");
        require(parent.madrassaId, "parent.madrassaId");
        require(parent.phone, "parent.phone");

        require(credential.id, "credential.id");
        require(credential.accountId, "credential.accountId");
        require(credential.madrassaId, "credential.madrassaId");
        require(credential.passwordHash, "credential.passwordHash");
        require(credential.passwordSalt, "credential.passwordSalt");

        if (credential.role != AccountRole.PARENT) {
            throw new IllegalArgumentException(
                    "Credential role must be PARENT."
            );
        }

        if (!parent.id.equals(credential.accountId)) {
            throw new IllegalArgumentException(
                    "Parent and credential account IDs differ."
            );
        }

        if (!parent.madrassaId.equals(
                credential.madrassaId
        )) {
            throw new IllegalArgumentException(
                    "Parent and credential Madrassa IDs differ."
            );
        }
    }

    private void require(
            String value,
            String name
    ) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    name + " is required."
            );
        }
    }
}
