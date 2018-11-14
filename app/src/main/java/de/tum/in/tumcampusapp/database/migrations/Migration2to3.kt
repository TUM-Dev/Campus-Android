package de.tum.`in`.tumcampusapp.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class Migration2to3 : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("DROP TABLE events")
        database.execSQL("CREATE TABLE IF NOT EXISTS `events` " +
                "(`id` INTEGER NOT NULL, " +
                "`image_url` TEXT, " +
                "`title` TEXT NOT NULL, " +
                "`description` TEXT NOT NULL, " +
                "`locality` TEXT NOT NULL, " +
                "`start_time` TEXT NOT NULL, " +
                "`end_time` TEXT, " +
                "`event_url` TEXT NOT NULL, " +
                "`dismissed` INTEGER NOT NULL, " +
                "`link` TEXT NOT NULL, " +
                "`tu_film` INTEGER NOT NULL, " +
                "PRIMARY KEY(`id`))")
    }
}