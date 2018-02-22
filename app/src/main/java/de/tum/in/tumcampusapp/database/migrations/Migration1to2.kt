package de.tum.`in`.tumcampusapp.database.migrations

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.migration.Migration

/**
 * We initialize our previous db that was not using room
 */
class Migration1to2 : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        //NOOP - our db is already on version 2 somehow. So this is just a dummy
    }
}