package com.zamcan.madrassa.data.repository;

import android.content.ContentValues;
import android.database.Cursor;

import com.zamcan.madrassa.data.local.EduNoorDatabase;
import com.zamcan.madrassa.data.model.StudentGroupMember;
import com.zamcan.madrassa.domain.repository.StudentGroupMemberStore;

import java.util.ArrayList;
import java.util.List;

public final class StudentGroupMemberRepository
        implements StudentGroupMemberStore {

    private final EduNoorDatabase database;

    public StudentGroupMemberRepository(
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
    public List<StudentGroupMember> findByGroup(
            String groupId
    ) {
        if (blank(groupId)) {
            return new ArrayList<>();
        }

        return find(
                "group_id = ?",
                new String[]{groupId.trim()}
        );
    }

    @Override
    public List<StudentGroupMember> findByStudent(
            String studentId
    ) {
        if (blank(studentId)) {
            return new ArrayList<>();
        }

        return find(
                "student_id = ?",
                new String[]{studentId.trim()}
        );
    }

    @Override
    public boolean exists(
            String groupId,
            String studentId
    ) {
        if (blank(groupId) || blank(studentId)) {
            return false;
        }

        Cursor cursor = database.getReadableDatabase().query(
                "student_group_members",
                new String[]{"group_id"},
                "group_id = ? AND student_id = ?",
                new String[]{
                        groupId.trim(),
                        studentId.trim()
                },
                null,
                null,
                null,
                "1"
        );

        try {
            return cursor.moveToFirst();
        } finally {
            cursor.close();
        }
    }

    @Override
    public boolean add(
            StudentGroupMember member
    ) {
        validate(member);

        ContentValues values = new ContentValues();

        values.put(
                "group_id",
                member.groupId.trim()
        );

        values.put(
                "student_id",
                member.studentId.trim()
        );

        values.put(
                "created_at",
                member.createdAt
        );

        return database.getWritableDatabase().insert(
                "student_group_members",
                null,
                values
        ) != -1;
    }

    @Override
    public boolean remove(
            String groupId,
            String studentId
    ) {
        if (blank(groupId) || blank(studentId)) {
            return false;
        }

        return database.getWritableDatabase().delete(
                "student_group_members",
                "group_id = ? AND student_id = ?",
                new String[]{
                        groupId.trim(),
                        studentId.trim()
                }
        ) == 1;
    }

    private List<StudentGroupMember> find(
            String selection,
            String[] args
    ) {
        List<StudentGroupMember> result =
                new ArrayList<>();

        Cursor cursor = database.getReadableDatabase().query(
                "student_group_members",
                null,
                selection,
                args,
                null,
                null,
                "created_at ASC"
        );

        try {
            while (cursor.moveToNext()) {
                StudentGroupMember value =
                        new StudentGroupMember();

                value.groupId = cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                "group_id"
                        )
                );

                value.studentId = cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                "student_id"
                        )
                );

                value.createdAt = cursor.getLong(
                        cursor.getColumnIndexOrThrow(
                                "created_at"
                        )
                );

                result.add(value);
            }
        } finally {
            cursor.close();
        }

        return result;
    }

    private static void validate(
            StudentGroupMember member
    ) {
        if (member == null
                || blank(member.groupId)
                || blank(member.studentId)) {
            throw new IllegalArgumentException(
                    "student group membership is incomplete"
            );
        }
    }

    private static boolean blank(String value) {
        return value == null
                || value.trim().isEmpty();
    }
}
