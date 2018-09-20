package de.tum.`in`.tumcampusapp.di

import android.app.Application
import android.arch.persistence.room.Room
import dagger.Module
import dagger.Provides
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.database.migrations.*
import javax.inject.Singleton

@Module
class InMemoryDatabaseModule(private val application: Application) {

    @Provides
    @Singleton
    fun providesDatabase(): TcaDb {
        return Room.inMemoryDatabaseBuilder(application.applicationContext, TcaDb::class.java)
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
