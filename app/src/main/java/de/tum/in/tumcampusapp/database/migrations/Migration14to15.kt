package de.tum.`in`.tumcampusapp.database.migrations

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.migration.Migration

class Migration14to15 : Migration(14, 15) {

    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE scheduled_notifications (" +
                "id INTEGER NOT NULL PRIMARY KEY, " +
                "type_id INTEGER NOT NULL, " +
                "content_id INTEGER NOT NULL)"
        )
    }

}