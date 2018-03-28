package de.tum.`in`.tumcampusapp.database.migrations

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.migration.Migration

/**
 * Those migrations were previously missing, v6 now ensures, that those tables are properly created
 */
class Migration5to6 : Migration(5, 6) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.apply {
            execSQL("CREATE TABLE IF NOT EXISTS `Cafeteria` (`distance` REAL NOT NULL, `id` INTEGER NOT NULL, `name` TEXT NOT NULL, `address` TEXT NOT NULL, `latitude` REAL NOT NULL, `longitude` REAL NOT NULL, PRIMARY KEY(`id`))")
            execSQL("CREATE TABLE IF NOT EXISTS `CafeteriaMenu` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `cafeteriaId` INTEGER NOT NULL, `date` TEXT, `typeShort` TEXT NOT NULL, `typeLong` TEXT NOT NULL, `typeNr` INTEGER NOT NULL, `name` TEXT NOT NULL)")
            execSQL("CREATE TABLE IF NOT EXISTS `FavoriteDish` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `cafeteriaId` INTEGER NOT NULL, `dishName` TEXT NOT NULL, `date` TEXT NOT NULL, `tag` TEXT NOT NULL)")
            execSQL("CREATE TABLE IF NOT EXISTS `Sync` (`id` TEXT NOT NULL, `lastSync` TEXT NOT NULL, PRIMARY KEY(`id`))")
            execSQL("CREATE TABLE IF NOT EXISTS `TumLock` (`url` TEXT NOT NULL, `error` TEXT NOT NULL, `timestamp` TEXT NOT NULL, `lockedFor` INTEGER NOT NULL, `active` INTEGER NOT NULL, PRIMARY KEY(`url`))")
            execSQL("CREATE TABLE IF NOT EXISTS `BuildingToGps` (`id` TEXT NOT NULL, `latitude` TEXT NOT NULL, `longitude` TEXT NOT NULL, PRIMARY KEY(`id`))")
            execSQL("CREATE TABLE IF NOT EXISTS `Location` (`id` INTEGER NOT NULL, `category` TEXT NOT NULL, `name` TEXT NOT NULL, `address` TEXT NOT NULL, `room` TEXT NOT NULL, `transport` TEXT NOT NULL, `hours` TEXT NOT NULL, `remark` TEXT NOT NULL, `url` TEXT NOT NULL, PRIMARY KEY(`id`))")
        }
    }
}