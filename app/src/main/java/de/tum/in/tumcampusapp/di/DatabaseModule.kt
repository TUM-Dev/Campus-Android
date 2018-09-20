package de.tum.`in`.tumcampusapp.di

import android.arch.persistence.room.Room.databaseBuilder
import android.content.Context
import dagger.Module
import dagger.Provides
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.database.migrations.*
import de.tum.`in`.tumcampusapp.utils.Const.DATABASE_NAME
import javax.inject.Singleton

@Module
class DatabaseModule(private val context: Context) {

    @Provides
    @Singleton
    fun providesDatabase(): TcaDb {
        return databaseBuilder(context.applicationContext, TcaDb::class.java, DATABASE_NAME)
                .allowMainThreadQueries()
                .addMigrations(*migrations)
                .fallbackToDestructiveMigration()
                .build()
    }

    companion object {

        private val migrations = arrayOf(
                Migration1to2(), Migration2to3(), Migration3to4(),
                Migration4to5(), Migration5to6(), Migration6to7(),
                Migration11to12(), Migration12to13(), Migration13to14(), Migration14to15()
        )

    }

}
