package de.tum.`in`.tumcampusapp.database.migrations

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.migration.Migration

class Migration13to14 : Migration(13, 14) {

    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
                "CREATE TABLE IF NOT EXISTS events (" +
                        "id INTEGER NOT NULL, " +
                        "image_url TEXT, " +
                        "title TEXT NOT NULL, " +
                        "description TEXT NOT NULL, " +
                        "locality TEXT NOT NULL, " +
                        "start_time TEXT NOT NULL, " +
                        "end_time TEXT, " +
                        "event_url TEXT NOT NULL, " +
                        "dismissed INTEGER NOT NULL, " +
                        "PRIMARY KEY(id))"
        )

        database.execSQL(
                "CREATE TABLE IF NOT EXISTS tickets (" +
                        "id INTEGER NOT NULL, " +
                        "event_id INTEGER NOT NULL, " +
                        "code TEXT NOT NULL, " +
                        "ticket_type_id INTEGER NOT NULL, " +
                        "redemption TEXT, " +
                        "PRIMARY KEY(id))"
        )

        database.execSQL(
                "CREATE TABLE IF NOT EXISTS ticket_types (" +
                        "id INTEGER NOT NULL, " +
                        "price INTEGER NOT NULL, " +
                        "description TEXT NOT NULL, " +
                        "PRIMARY KEY(id))"
        )
    }

}