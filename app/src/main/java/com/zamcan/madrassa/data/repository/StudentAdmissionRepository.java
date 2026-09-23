package com.zamcan.madrassa.data.repository;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.zamcan.madrassa.data.local.EduNoorDatabase;
import com.zamcan.madrassa.data.model.Student;
import com.zamcan.madrassa.data.local.DatabaseTransactionRunner;
import com.zamcan.madrassa.domain.repository.StudentAdmissionStore;

public final class StudentAdmissionRepository
        implements StudentAdmissionStore {

    private final EduNoorDatabase database;
    private final DatabaseTransactionRunner transactions;

    public StudentAdmissionRepository(
            EduNoorDatabase database
    ) {
        if (database == null) {
            throw new IllegalArgumentException(
                    "database is required"
            );
        }

        this.database = database;
        this.transactions =
                new DatabaseTransactionRunner(database);
    }

    @Override
    public void admit(
            Student student,
            String parentId
    ) {
        validate(student, parentId);

        transactions.run(
                new DatabaseTransactionRunner.TransactionWork() {
                    @Override
                    public void execute(
                            SQLiteDatabase db
                    ) {
                        ContentValues studentValues =
                                new ContentValues();

                        studentValues.put(
                                "id",
                                student.id.trim()
                        );

                        studentValues.put(
                                "madrassa_id",
                                student.madrassaId.trim()
                        );

                        studentValues.put(
                                "parent_id",
                                parentId.trim()
                        );

                        studentValues.put(
                                "full_name",
                                student.fullName.trim()
                        );

                        studentValues.put(
                                "parent_guardian_name",
                                student.parentGuardianName.trim()
                        );

                        studentValues.put(
                                "main_phone",
                                normalizePhone(
                                        student.mainPhone
                                )
                        );

                        if (isBlank(
                                student.emergencyPhone
                        )) {
                            studentValues.putNull(
                                    "emergency_phone"
                            );
                        } else {
                            studentValues.put(
                                    "emergency_phone",
                                    normalizePhone(
                                            student.emergencyPhone
                                    )
                            );
                        }

                        studentValues.put(
                                "class_id",
                                student.classId.trim()
                        );

                        studentValues.put(
                                "quran_level",
                                student.quranLevel.trim()
                        );

                        studentValues.put(
                                "hifz_level",
                                student.hifzLevel.trim()
                        );

                        studentValues.put(
                                "active",
                                student.active ? 1 : 0
                        );

                        db.insertOrThrow(
                                "students",
                                null,
                                studentValues
                        );

                        ContentValues link =
                                new ContentValues();

                        link.put(
                                "parent_id",
                                parentId.trim()
                        );

                        link.put(
                                "student_id",
                                student.id.trim()
                        );

                        link.put(
                                "created_at",
                                System.currentTimeMillis()
                        );

                        db.insertOrThrow(
                                "parent_student_links",
                                null,
                                link
                        );

                        if (student.programmeIds != null) {
                            for (String programmeId :
                                    student.programmeIds) {

                                if (isBlank(programmeId)) {
                                    continue;
                                }

                                ContentValues programme =
                                        new ContentValues();

                                programme.put(
                                        "student_id",
                                        student.id.trim()
                                );

                                programme.put(
                                        "programme_id",
                                        programmeId.trim()
                                );

                                programme.put(
                                        "created_at",
                                        System.currentTimeMillis()
                                );

                                db.insertOrThrow(
                                        "student_programmes",
                                        null,
                                        programme
                                );
                            }
                        }
                    }
                }
        );
    }

    private void validate(
            Student student,
            String parentId
    ) {
        if (student == null) {
            throw new IllegalArgumentException(
                    "student is required"
            );
        }

        if (isBlank(parentId)) {
            throw new IllegalArgumentException(
                    "parentId is required"
            );
        }

        if (isBlank(student.id)
                || isBlank(student.madrassaId)
                || isBlank(student.parentId)
                || !student.parentId.equals(
                parentId.trim()
        )) {
            throw new IllegalArgumentException(
                    "Student parent relationship is invalid."
            );
        }
    }

    private static String normalizePhone(
            String phone
    ) {
        String value = phone.trim()
                .replace(" ", "")
                .replace("-", "")
                .replace("(", "")
                .replace(")", "")
                .replace(".", "");

        if (value.startsWith("00")) {
            value = "+" + value.substring(2);
        }

        return value;
    }

    private static boolean isBlank(
            String value
    ) {
        return value == null
                || value.trim().isEmpty();
    }
}
