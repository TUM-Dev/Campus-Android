package de.tum.`in`.tumcampusapp.di

import android.arch.persistence.room.Room
import android.content.Context
import dagger.Module
import dagger.Provides
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.database.migrations.*
import javax.inject.Singleton

@Module
class InMemoryDatabaseModule(private val context: Context) {

    @Provides
    @Singleton
    fun providesDatabase(): TcaDb {
        return Room.inMemoryDatabaseBuilder(context.applicationContext, TcaDb::class.java)
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
