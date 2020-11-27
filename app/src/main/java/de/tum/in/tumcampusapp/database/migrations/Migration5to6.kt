package de.tum.`in`.tumcampusapp.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class Migration5to6 : Migration(5, 6) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE IF NOT EXISTS `eventSeriesMappings` " +
                "(`id` INTEGER NOT NULL, " +
                "`seriesId` Text NOT NULL, " +
                "`eventId` INTEGER NOT NULL, " +
                "PRIMARY KEY(`id`))")
    }
}