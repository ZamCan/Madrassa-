package com.zamcan.madrassa.data.repository;

import android.content.ContentValues;
import android.database.Cursor;

import com.zamcan.madrassa.data.local.EduNoorDatabase;
import com.zamcan.madrassa.data.model.LearningProgress;
import com.zamcan.madrassa.data.model.ProgressStatus;
import com.zamcan.madrassa.domain.repository.LearningProgressStore;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite persistence for learning progress.
 *
 * <p>The domain enum is the source of truth. Older databases may still
 * contain the v6 {@code state} column, so writes mirror the status into
 * that column when it exists without making the repository depend on a
 * particular schema version.</p>
 */
public final class LearningProgressRepository
        implements LearningProgressStore {

    private final EduNoorDatabase database;
    private Boolean legacyStateColumn;

    public LearningProgressRepository(EduNoorDatabase database) {
        if (database == null) {
            throw new IllegalArgumentException("database is required");
        }
        this.database = database;
    }

    @Override
    public LearningProgress findById(String id) {
        if (blank(id)) {
            return null;
        }

        Cursor cursor = database.getReadableDatabase().query(
                "learning_progress",
                null,
                "id = ?",
                new String[]{id.trim()},
                null,
                null,
                null,
                "1"
        );

        try {
            return cursor.moveToFirst() ? map(cursor) : null;
        } finally {
            cursor.close();
        }
    }

    @Override
    public List<LearningProgress> findByLearner(String learnerId) {
        return find("learner_id = ?", learnerId);
    }

    @Override
    public List<LearningProgress> findByLesson(String lessonId) {
        return find("lesson_id = ?", lessonId);
    }

    @Override
    public LearningProgress findByLearnerAndLesson(
            String learnerId,
            String lessonId
    ) {
        if (blank(learnerId) || blank(lessonId)) {
            return null;
        }

        Cursor cursor = database.getReadableDatabase().query(
                "learning_progress",
                null,
                "learner_id = ? AND lesson_id = ?",
                new String[]{learnerId.trim(), lessonId.trim()},
                null,
                null,
                "updated_at DESC",
                "1"
        );

        try {
            return cursor.moveToFirst() ? map(cursor) : null;
        } finally {
            cursor.close();
        }
    }

    private List<LearningProgress> find(
            String selection,
            String id
    ) {
        List<LearningProgress> result = new ArrayList<>();

        if (blank(id)) {
            return result;
        }

        Cursor cursor = database.getReadableDatabase().query(
                "learning_progress",
                null,
                selection,
                new String[]{id.trim()},
                null,
                null,
                "updated_at DESC"
        );

        try {
            while (cursor.moveToNext()) {
                result.add(map(cursor));
            }
        } finally {
            cursor.close();
        }

        return result;
    }

    @Override
    public boolean save(LearningProgress progress) {
        validate(progress);
        return database.getWritableDatabase().insert(
                "learning_progress",
                null,
                values(progress)
        ) != -1;
    }

    @Override
    public boolean update(LearningProgress progress) {
        validate(progress);
        return database.getWritableDatabase().update(
                "learning_progress",
                values(progress),
                "id = ?",
                new String[]{progress.id.trim()}
        ) == 1;
    }

    private LearningProgress map(Cursor cursor) {
        LearningProgress progress = new LearningProgress();

        progress.id = cursor.getString(
                cursor.getColumnIndexOrThrow("id")
        );
        progress.learnerId = cursor.getString(
                cursor.getColumnIndexOrThrow("learner_id")
        );
        progress.lessonId = cursor.getString(
                cursor.getColumnIndexOrThrow("lesson_id")
        );
        progress.madrassaId = text(cursor, "madrassa_id");
        progress.status = parse(cursor.getString(
                cursor.getColumnIndexOrThrow("status")
        ));
        progress.updatedAt = cursor.getLong(
                cursor.getColumnIndexOrThrow("updated_at")
        );

        return progress;
    }

    private ContentValues values(LearningProgress progress) {
        ContentValues values = new ContentValues();

        values.put("id", progress.id.trim());
        values.put("learner_id", progress.learnerId.trim());
        values.put("lesson_id", progress.lessonId.trim());

        if (progress.madrassaId == null) {
            values.putNull("madrassa_id");
        } else {
            values.put("madrassa_id", progress.madrassaId.trim());
        }

        values.put("status", progress.status.name());

        // Compatibility with the v6 state column. It is intentionally
        // detected once per repository instance rather than per write.
        if (hasLegacyStateColumn()) {
            values.put("state", progress.status.name());
        }

        values.put("updated_at", progress.updatedAt);
        return values;
    }

    private boolean hasLegacyStateColumn() {
        if (legacyStateColumn != null) {
            return legacyStateColumn;
        }

        Cursor cursor = database.getReadableDatabase().rawQuery(
                "PRAGMA table_info(learning_progress)",
                null
        );

        try {
            int nameIndex = cursor.getColumnIndex("name");

            while (cursor.moveToNext()) {
                if (nameIndex >= 0
                        && "state".equalsIgnoreCase(
                        cursor.getString(nameIndex))) {
                    legacyStateColumn = Boolean.TRUE;
                    return true;
                }
            }

            legacyStateColumn = Boolean.FALSE;
            return false;
        } finally {
            cursor.close();
        }
    }

    private static ProgressStatus parse(String value) {
        if ("STARTED".equalsIgnoreCase(value)) {
            return ProgressStatus.IN_PROGRESS;
        }

        try {
            return ProgressStatus.valueOf(value);
        } catch (Exception ignored) {
            return ProgressStatus.NOT_STARTED;
        }
    }

    private static String text(Cursor cursor, String name) {
        int index = cursor.getColumnIndex(name);
        return index < 0 || cursor.isNull(index)
                ? null
                : cursor.getString(index);
    }

    private static boolean blank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static void validate(LearningProgress progress) {
        if (progress == null
                || blank(progress.id)
                || blank(progress.learnerId)
                || blank(progress.lessonId)
                || progress.status == null) {
            throw new IllegalArgumentException(
                    "learning progress data is incomplete"
            );
        }
    }
}
