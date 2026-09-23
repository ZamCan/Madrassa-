package com.zamcan.madrassa.data.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.zamcan.madrassa.data.local.DatabaseTransactionRunner;
import com.zamcan.madrassa.data.local.EduNoorDatabase;
import com.zamcan.madrassa.data.model.AccountCredential;
import com.zamcan.madrassa.data.model.ApprovalStatus;
import com.zamcan.madrassa.data.model.Madrassa;
import com.zamcan.madrassa.data.model.PendingRegistrationCredential;
import com.zamcan.madrassa.data.model.Ustadh;
import com.zamcan.madrassa.domain.repository.RegistrationTransactionStore;

public final class RegistrationTransactionRepository
        implements RegistrationTransactionStore {

    private final EduNoorDatabase database;
    private final DatabaseTransactionRunner transactions;

    public RegistrationTransactionRepository(Context context) {
        if (context == null) {
            throw new IllegalArgumentException(
                    "context must not be null"
            );
        }

        database = new EduNoorDatabase(
                context.getApplicationContext()
        );

        transactions = new DatabaseTransactionRunner(
                database
        );
    }

    @Override
    public void submit(
            Madrassa madrassa,
            PendingRegistrationCredential pendingCredential
    ) {
        validateSubmission(madrassa, pendingCredential);

        transactions.run(db -> {

            Cursor existing = db.query(
                    "madrassas",
                    new String[]{"approval_status"},
                    "id = ?",
                    new String[]{madrassa.id},
                    null,
                    null,
                    null,
                    "1"
            );

            boolean exists = false;
            String existingStatus = null;

            try {
                if (existing.moveToFirst()) {
                    exists = true;
                    existingStatus = existing.getString(
                            existing.getColumnIndexOrThrow("approval_status")
                    );
                }
            } finally {
                existing.close();
            }

            /*
             * New registration.
             */
            if (!exists) {
                insertMadrassa(db, madrassa);
            }

            /*
             * Existing DRAFT/REJECTED registration.
             *
             * These states are intentionally allowed to resubmit.
             * Other states must not be overwritten.
             */
            else {
                if (!"DRAFT".equals(existingStatus)
                        && !"REJECTED".equals(existingStatus)) {

                    throw new IllegalStateException(
                            "Madrassa registration cannot be resubmitted from status: "
                                    + existingStatus
                    );
                }

                updateMadrassaForResubmission(db, madrassa);
            }

            /*
             * There can only be one pending Ustadh credential per
             * Madrassa. Update it when it already exists; otherwise
             * insert it.
             */
            int updated = db.update(
                    "pending_registration_credentials",
                    pendingValues(pendingCredential),
                    "madrassa_id = ? AND role = ?",
                    new String[]{
                            pendingCredential.madrassaId,
                            pendingCredential.role.name()
                    }
            );

            if (updated == 0) {
                db.insertOrThrow(
                        "pending_registration_credentials",
                        null,
                        pendingValues(pendingCredential)
                );
            }
        });
    }

    private void updateMadrassaForResubmission(
            SQLiteDatabase db,
            Madrassa madrassa
    ) {
        int updated = db.update(
                "madrassas",
                madrassaValues(madrassa),
                "id = ?",
                new String[]{madrassa.id}
        );

        if (updated != 1) {
            throw new IllegalStateException(
                    "Expected one Madrassa registration to be updated."
            );
        }
    }


    @Override
    public void activate(
            Madrassa madrassa,
            Ustadh ustadh,
            AccountCredential credential,
            PendingRegistrationCredential pendingCredential
    ) {
        validateActivation(
                madrassa,
                ustadh,
                credential,
                pendingCredential
        );

        transactions.run(db -> {
            insertUstadh(db, ustadh);
            insertCredential(db, credential);

            ContentValues madrassaValues =
                    new ContentValues();

            madrassaValues.put(
                    "head_ustadh_id",
                    ustadh.id
            );

            madrassaValues.put(
                    "approval_status",
                    "ACTIVE"
            );

            int updated = db.update(
                    "madrassas",
                    madrassaValues,
                    "id = ? AND approval_status = ?",
                    new String[]{
                            madrassa.id,
                            "APPROVED"
                    }
            );

            if (updated != 1) {
                throw new IllegalStateException(
                        "Madrassa activation update failed."
                );
            }

            int deleted = db.delete(
                    "pending_registration_credentials",
                    "madrassa_id = ? AND role = ?",
                    new String[]{
                            madrassa.id,
                            pendingCredential.role.name()
                    }
            );

            if (deleted != 1) {
                throw new IllegalStateException(
                        "Pending registration credential could not be consumed."
                );
            }
        });
    }

    private void insertMadrassa(
            SQLiteDatabase db,
            Madrassa madrassa
    ) {
        ContentValues values =
                new ContentValues();

        values.put("id", madrassa.id);
        values.put("name", madrassa.name);
        values.put("type", madrassa.type);
        values.put(
                "administration_type",
                madrassa.administrationType
        );
        values.put("region", madrassa.region);
        values.put("district", madrassa.district);
        values.put("ward", madrassa.ward);
        values.put("area", madrassa.area);
        values.put(
                "nearby_landmark",
                madrassa.nearbyLandmark
        );
        values.put(
                "masjid_name",
                madrassa.masjidName
        );
        values.put(
                "masjid_location",
                madrassa.masjidLocation
        );
        values.put(
                "head_ustadh_id",
                madrassa.headUstadhId
        );
        values.put(
                "phone",
                madrassa.phone
        );
        values.put(
                "secondary_phone",
                madrassa.secondaryPhone
        );
        values.put("email", madrassa.email);
        values.put(
                "ustadh_count",
                madrassa.ustadhCount
        );
        values.put(
                "approval_status",
                madrassa.approvalStatus.name()
        );
        values.put(
                "rejection_reason",
                madrassa.rejectionReason
        );
        values.put(
                "submitted_at",
                madrassa.submittedAt
        );
        values.put(
                "approved_at",
                madrassa.approvedAt
        );

        db.insertOrThrow(
                "madrassas",
                null,
                values
        );
    }

    private void insertPendingCredential(
            SQLiteDatabase db,
            PendingRegistrationCredential credential
    ) {
        ContentValues values =
                new ContentValues();

        values.put("id", credential.id);
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

        db.insertOrThrow(
                "pending_registration_credentials",
                null,
                values
        );
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
        values.put("phone", ustadh.phone);
        values.put("email", ustadh.email);
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

        values.put("id", credential.id);
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

    private ContentValues madrassaValues(Madrassa madrassa) {
        ContentValues values = new ContentValues();

        values.put("id", madrassa.id);
        values.put("name", madrassa.name);
        values.put("type", madrassa.type);
        values.put("administration_type", madrassa.administrationType);
        values.put("region", madrassa.region);
        values.put("district", madrassa.district);
        values.put("ward", madrassa.ward);
        values.put("area", madrassa.area);
        values.put("nearby_landmark", madrassa.nearbyLandmark);
        values.put("masjid_name", madrassa.masjidName);
        values.put("masjid_location", madrassa.masjidLocation);
        values.put("head_ustadh_id", madrassa.headUstadhId);
        values.put("phone", madrassa.phone);
        values.put("secondary_phone", madrassa.secondaryPhone);
        values.put("email", madrassa.email);
        values.put("ustadh_count", madrassa.ustadhCount);
        values.put(
                "approval_status",
                madrassa.approvalStatus == null
                        ? ApprovalStatus.SUBMITTED.name()
                        : madrassa.approvalStatus.name()
        );
        values.put("rejection_reason", madrassa.rejectionReason);
        values.put("submitted_at", madrassa.submittedAt);
        values.put("approved_at", madrassa.approvedAt);

        return values;
    }

    private ContentValues pendingValues(
            PendingRegistrationCredential pending
    ) {
        ContentValues values = new ContentValues();

        values.put("id", pending.id);
        values.put("madrassa_id", pending.madrassaId);
        values.put("role", pending.role.name());
        values.put("password_hash", pending.passwordHash);
        values.put("password_salt", pending.passwordSalt);
        values.put("created_at", pending.createdAt);
        values.put("updated_at", pending.updatedAt);

        return values;
    }

    private void validateSubmission(
            Madrassa madrassa,
            PendingRegistrationCredential pending
    ) {
        if (madrassa == null ||
                isBlank(madrassa.id)) {
            throw new IllegalArgumentException(
                    "Madrassa is required."
            );
        }

        if (pending == null ||
                isBlank(pending.id)) {
            throw new IllegalArgumentException(
                    "Pending credential is required."
            );
        }

        if (!madrassa.id.equals(
                pending.madrassaId
        )) {
            throw new IllegalArgumentException(
                    "Pending credential tenant mismatch."
            );
        }
    }

    private void validateActivation(
            Madrassa madrassa,
            Ustadh ustadh,
            AccountCredential credential,
            PendingRegistrationCredential pending
    ) {
        validateSubmission(
                madrassa,
                pending
        );

        if (ustadh == null ||
                isBlank(ustadh.id) ||
                !ustadh.headUstadh ||
                !ustadh.active) {
            throw new IllegalArgumentException(
                    "Valid active Head Ustadh is required."
            );
        }

        if (!madrassa.id.equals(
                ustadh.madrassaId
        )) {
            throw new IllegalArgumentException(
                    "Ustadh tenant mismatch."
            );
        }

        if (credential == null ||
                isBlank(credential.id) ||
                credential.role == null) {
            throw new IllegalArgumentException(
                    "Valid credential is required."
            );
        }

        if (!ustadh.id.equals(
                credential.accountId
        )) {
            throw new IllegalArgumentException(
                    "Credential account mismatch."
            );
        }

        if (!madrassa.id.equals(
                credential.madrassaId
        )) {
            throw new IllegalArgumentException(
                    "Credential tenant mismatch."
            );
        }

        if (credential.role != pending.role) {
            throw new IllegalArgumentException(
                    "Credential role mismatch."
            );
        }
    }

    private static boolean isBlank(String value) {
        return value == null ||
                value.trim().isEmpty();
    }
}
