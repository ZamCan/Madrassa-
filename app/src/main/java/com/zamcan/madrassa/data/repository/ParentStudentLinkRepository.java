package com.zamcan.madrassa.data.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.zamcan.madrassa.data.local.EduNoorDatabase;
import com.zamcan.madrassa.domain.common.TenantPolicy;
import com.zamcan.madrassa.domain.repository.ParentStudentLinkStore;

public final class ParentStudentLinkRepository
        implements ParentStudentLinkStore {

    private final EduNoorDatabase database;

    public ParentStudentLinkRepository(
            EduNoorDatabase database
    ) {
        if (database == null) {
            throw new IllegalArgumentException(
                    "database is required"
            );
        }

        this.database = database;
    }

    @Override
    public void link(
            String parentId,
            String studentId
    ) {
        require(parentId, "parentId");
        require(studentId, "studentId");

        SQLiteDatabase db =
                database.getWritableDatabase();

        db.beginTransaction();

        try {
            ParentStudentData data =
                    loadRelationshipData(
                            db,
                            parentId.trim(),
                            studentId.trim()
                    );

            if (data.parentMadrassaId == null
                    || data.studentMadrassaId == null) {
                throw new SecurityException(
                        "Parent and student must belong to a Madrassa."
                );
            }

            if (!TenantPolicy.sameMadrassa(
                    data.parentMadrassaId,
                    data.studentMadrassaId
            )) {
                throw new SecurityException(
                        "Cross-Madrassa student linking is forbidden."
                );
            }

            if (data.studentParentId != null
                    && !data.studentParentId.equals(
                    parentId.trim()
            )) {
                throw new IllegalStateException(
                        "Student is already linked to another parent."
                );
            }

            ContentValues link =
                    new ContentValues();

            link.put(
                    "parent_id",
                    parentId.trim()
            );

            link.put(
                    "student_id",
                    studentId.trim()
            );

            link.put(
                    "created_at",
                    System.currentTimeMillis()
            );

            db.insertWithOnConflict(
                    "parent_student_links",
                    null,
                    link,
                    SQLiteDatabase.CONFLICT_IGNORE
            );

            ContentValues student =
                    new ContentValues();

            student.put(
                    "parent_id",
                    parentId.trim()
            );

            int affected = db.update(
                    "students",
                    student,
                    "id = ?",
                    new String[]{studentId.trim()}
            );

            if (affected != 1) {
                throw new IllegalStateException(
                        "Student relationship update failed."
                );
            }

            db.setTransactionSuccessful();

        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void unlink(
            String parentId,
            String studentId
    ) {
        require(parentId, "parentId");
        require(studentId, "studentId");

        SQLiteDatabase db =
                database.getWritableDatabase();

        db.beginTransaction();

        try {
            ParentStudentData data =
                    loadRelationshipData(
                            db,
                            parentId.trim(),
                            studentId.trim()
                    );

            if (data.studentParentId == null
                    || !data.studentParentId.equals(
                    parentId.trim()
            )) {
                throw new SecurityException(
                        "Parent does not own this student."
                );
            }

            if (!TenantPolicy.sameMadrassa(
                    data.parentMadrassaId,
                    data.studentMadrassaId
            )) {
                throw new SecurityException(
                        "Cross-Madrassa student relationship is forbidden."
                );
            }

            db.delete(
                    "parent_student_links",
                    "parent_id = ? AND student_id = ?",
                    new String[]{
                            parentId.trim(),
                            studentId.trim()
                    }
            );

            ContentValues values =
                    new ContentValues();

            values.putNull("parent_id");

            db.update(
                    "students",
                    values,
                    "id = ? AND parent_id = ?",
                    new String[]{
                            studentId.trim(),
                            parentId.trim()
                    }
            );

            db.setTransactionSuccessful();

        } finally {
            db.endTransaction();
        }
    }

    private ParentStudentData loadRelationshipData(
            SQLiteDatabase db,
            String parentId,
            String studentId
    ) {
        ParentStudentData data =
                new ParentStudentData();

        Cursor parent = db.rawQuery(
                "SELECT madrassa_id, active " +
                        "FROM parents " +
                        "WHERE id = ? " +
                        "LIMIT 1",
                new String[]{parentId}
        );

        try {
            if (!parent.moveToFirst()) {
                throw new IllegalArgumentException(
                        "Parent not found."
                );
            }

            data.parentMadrassaId =
                    parent.isNull(0)
                            ? null
                            : parent.getString(0);

            if (parent.getInt(1) != 1) {
                throw new SecurityException(
                        "Parent is inactive."
                );
            }
        } finally {
            parent.close();
        }

        Cursor student = db.rawQuery(
                "SELECT madrassa_id, parent_id, active " +
                        "FROM students " +
                        "WHERE id = ? " +
                        "LIMIT 1",
                new String[]{studentId}
        );

        try {
            if (!student.moveToFirst()) {
                throw new IllegalArgumentException(
                        "Student not found."
                );
            }

            data.studentMadrassaId =
                    student.getString(0);

            data.studentParentId =
                    student.isNull(1)
                            ? null
                            : student.getString(1);

            if (student.getInt(2) != 1) {
                throw new SecurityException(
                        "Student is inactive."
                );
            }

        } finally {
            student.close();
        }

        return data;
    }

    private static final class ParentStudentData {
        String parentMadrassaId;
        String studentMadrassaId;
        String studentParentId;
    }

    private static void require(
            String value,
            String field
    ) {
        if (value == null
                || value.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    field + " is required"
            );
        }
    }
}
