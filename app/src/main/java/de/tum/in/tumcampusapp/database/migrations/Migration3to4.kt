package de.tum.`in`.tumcampusapp.database.migrations

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.migration.Migration
import android.database.SQLException

class Migration3to4 : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("DROP TABLE IF EXISTS  chat_message")
        database.execSQL("CREATE TABLE chat_message (" +
                "_id INTEGER NOT NULL PRIMARY KEY," +
                "previous INTEGER NOT NULL, " +
                "room INTEGER NOT NULL, " +
                "text TEXT NULL, " +
                "timestamp TEXT NULL, " +
                "signature TEXT NULL," +
                "member TEXT NULL, " +
                "read INTEGER NOT NULL, " +
                "sending INTEGER NOT NULL " +
                ")")
    }

}