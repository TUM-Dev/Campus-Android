package de.tum.`in`.tumcampusapp.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class Migration3to4 : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) =
            database.execSQL("DROP TABLE IF EXISTS wifi_measurement")
}
