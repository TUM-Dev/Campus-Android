package de.tum.`in`.tumcampusapp.component.ui.tufilm.di

import dagger.Module
import dagger.Provides
import de.tum.`in`.tumcampusapp.component.ui.tufilm.repository.KinoLocalRepository
import de.tum.`in`.tumcampusapp.database.TcaDb

@Module
class KinoModule {

    @Provides
    fun provideKinoLocalRepository(
            database: TcaDb
    ): KinoLocalRepository = KinoLocalRepository(database)

}
