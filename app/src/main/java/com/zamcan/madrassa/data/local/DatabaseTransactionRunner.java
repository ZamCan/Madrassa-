package com.zamcan.madrassa.data.local;

import android.database.sqlite.SQLiteDatabase;

public final class DatabaseTransactionRunner {

    private final EduNoorDatabase database;

    public DatabaseTransactionRunner(
            EduNoorDatabase database
    ) {
        if (database == null) {
            throw new IllegalArgumentException(
                    "database must not be null"
            );
        }

        this.database = database;
    }

    public void run(
            TransactionWork work
    ) {
        if (work == null) {
            throw new IllegalArgumentException(
                    "work must not be null"
            );
        }

        SQLiteDatabase db =
                database.getWritableDatabase();

        db.beginTransaction();

        try {
            work.execute(db);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public interface TransactionWork {

        void execute(
                SQLiteDatabase database
        );
    }
}
