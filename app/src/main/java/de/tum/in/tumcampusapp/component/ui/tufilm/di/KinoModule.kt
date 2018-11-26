package de.tum.`in`.tumcampusapp.component.ui.tufilm.di

import dagger.Module
import dagger.Provides
import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.component.ui.tufilm.KinoUpdater
import de.tum.`in`.tumcampusapp.component.ui.tufilm.repository.KinoLocalRepository
import de.tum.`in`.tumcampusapp.component.ui.tufilm.repository.KinoRemoteRepository
import de.tum.`in`.tumcampusapp.database.TcaDb

@Module
class KinoModule {

    @Provides
    fun provideKinoLocalRepository(
            database: TcaDb
    ): KinoLocalRepository {
        return KinoLocalRepository(database)
    }

    @Provides
    fun provideKinoRemoteRepository(
            tumCabeClient: TUMCabeClient
    ): KinoRemoteRepository {
        return KinoRemoteRepository(tumCabeClient)
    }

    @Provides
    fun provideKinoUpdater(
            localRepository: KinoLocalRepository,
            remoteRepository: KinoRemoteRepository
    ): KinoUpdater {
        return KinoUpdater(localRepository, remoteRepository)
    }

}
