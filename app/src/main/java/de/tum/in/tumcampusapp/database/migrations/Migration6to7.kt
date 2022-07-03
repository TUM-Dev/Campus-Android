package de.tum.`in`.tumcampusapp.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class Migration6to7 : Migration(6, 7) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "CREATE TABLE IF NOT EXISTS `event_color_table` " +
                "(`eventColorId` INTEGER, " +
                "`event_identifier` TEXT NOT NULL, " +
                "`event_nr` TEXT NOT NULL, " +
                "`is_single_event` INTEGER NOT NULL, " +
                "`color` INTEGER NOT NULL, " +
                "PRIMARY KEY(`eventColorId`))"
        )
    }
}
