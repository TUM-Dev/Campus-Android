package de.tum.`in`.tumcampusapp.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class Migration7to8 : Migration(7, 8) {

    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("DROP TABLE IF EXISTS `Cafeteria`")

        database.execSQL("CREATE TABLE IF NOT EXISTS `Cafeteria` (" +
                "`id` TEXT NOT NULL," +
                "`name` TEXT NOT NULL," +
                "`address` TEXT NOT NULL," +
                "`latitude` REAL NOT NULL," +
                "`longitude` REAL NOT NULL," +
                "`distance` REAL NOT NULL," +
                "PRIMARY KEY(`id`))")
    }
}