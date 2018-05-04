package de.tum.`in`.tumcampusapp.database.migrations

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.migration.Migration

/**
 * We removed the quiz function from the app
 */
class Migration6to7 : Migration(6, 7) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE chat_room ADD last_read INTEGER NOT NULL DEFAULT -1")

        // this the statement below is not possible in SQLite, recreate the chat_message
        //database.execSQL("ALTER TABLE chat_message DROP COLUMN read")
        database.execSQL("DROP TABLE IF EXISTS  chat_message")
        database.execSQL("CREATE TABLE chat_message (" +
                "_id INTEGER NOT NULL PRIMARY KEY," +
                "previous INTEGER NOT NULL, " +
                "room INTEGER NOT NULL, " +
                "text TEXT NULL, " +
                "timestamp TEXT NULL, " +
                "signature TEXT NULL," +
                "member TEXT NULL, " +
                "sending INTEGER NOT NULL " +
                ")")
    }
}