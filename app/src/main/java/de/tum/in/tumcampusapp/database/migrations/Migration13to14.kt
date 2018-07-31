package de.tum.`in`.tumcampusapp.database.migrations

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.migration.Migration

class Migration13to14 : Migration(13, 14) {

    override fun migrate(database: SupportSQLiteDatabase) {
        // Create the new table that stores scheduled notifications
        database.execSQL("CREATE TABLE scheduled_notifications (" +
                "id INTEGER NOT NULL PRIMARY KEY, " +
                "type_id INTEGER NOT NULL, " +
                "content_id INTEGER NOT NULL)"
        )
    }

}