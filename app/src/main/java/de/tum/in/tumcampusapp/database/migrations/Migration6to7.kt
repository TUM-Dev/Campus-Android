package de.tum.`in`.tumcampusapp.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class Migration6to7 : Migration(6, 7) {

    override fun migrate(database: SupportSQLiteDatabase) {
        // Remove old table. This leads to data loss, but in this case the data is either:
        //  *) re-loaded via the API, which overrides all the data in the table
        //  *) uses cache. But, the DAO uses REPLACE as insertion strategy, meaning in this case
        //      all data is overridden too
        //  This means no "actual" data loss occurs by dropping the old table
        database.execSQL("DROP TABLE IF EXISTS `CafeteriaMenu`")

        // Create new table with updated new fields
        database.execSQL("CREATE TABLE IF NOT EXISTS `CafeteriaMenu` (" +
                "`menuId` INTEGER NOT NULL," +
                "`cafeteriaId` INTEGER NOT NULL," +
                "`slug` TEXT NOT NULL," +
                "`date` TEXT," +
                "`name` TEXT NOT NULL," +
                "`dishType` TEXT NOT NULL," +
                "`labels` TEXT NOT NULL," +
                "`calendarWeek` INTEGER NOT NULL," +
                "PRIMARY KEY(`menuId`))")

        // Same as above for Cafeteria
        database.execSQL("DROP TABLE IF EXISTS `Cafeteria`")

        database.execSQL("CREATE TABLE IF NOT EXISTS `Cafeteria` (" +
                "`id` INTEGER NOT NULL," +
                "`slug` TEXT NOT NULL," +
                "`name` TEXT NOT NULL," +
                "`address` TEXT NOT NULL," +
                "`latitude` REAL NOT NULL," +
                "`longitude` REAL NOT NULL," +
                "`distance` REAL NOT NULL," +
                "PRIMARY KEY(`id`))")

        // Reset location, to get rid of old references && Fav Dish
    }
}