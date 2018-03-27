package de.tum.`in`.tumcampusapp.database.migrations

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.migration.Migration

/**
 * We removed the quiz function from the app
 */
class Migration4to5 : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("DROP TABLE IF EXISTS  ownQuestions")
        database.execSQL("DROP TABLE IF EXISTS  openQuestions")
    }

}