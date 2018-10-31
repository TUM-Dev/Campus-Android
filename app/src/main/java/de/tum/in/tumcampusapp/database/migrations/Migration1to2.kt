package de.tum.`in`.tumcampusapp.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class Migration1to2 : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE active_alarms (id INTEGER NOT NULL PRIMARY KEY)")
    }
}