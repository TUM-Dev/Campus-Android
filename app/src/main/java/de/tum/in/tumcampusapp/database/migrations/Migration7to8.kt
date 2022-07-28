package de.tum.`in`.tumcampusapp.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class Migration7to8 : Migration(7, 8) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("DROP TABLE IF EXISTS `chat_message`")
        database.execSQL("DROP TABLE IF EXISTS `chat_room`")
    }
}