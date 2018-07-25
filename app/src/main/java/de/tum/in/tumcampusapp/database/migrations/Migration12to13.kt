package de.tum.`in`.tumcampusapp.database.migrations

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.migration.Migration

class Migration12to13 : Migration(12, 13) {

    override fun migrate(database: SupportSQLiteDatabase) {
        // Rename occupied_till to occupied_until by creating an updated version of study_rooms
        // Also add free_until column
        database.execSQL("ALTER TABLE study_rooms RENAME TO tmp_study_rooms")
        database.execSQL("CREATE TABLE study_rooms " +
                "(id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "code TEXT NOT NULL, " +
                "name TEXT NOT NULL, " +
                "building_name TEXT NOT NULL, " +
                "group_id INTEGER NOT NULL, " +
                "occupied_until TEXT, " +
                "free_until TEXT)")
        database.execSQL("INSERT INTO study_rooms(id, code, name, building_name, group_id, occupied_until) SELECT id, code, name, location, group_id, occupied_till FROM tmp_study_rooms")
        database.execSQL("DROP TABLE tmp_study_rooms")
    }

}