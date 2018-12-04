package de.tum.`in`.tumcampusapp.component.ui.tufilm.di

import dagger.Module
import dagger.Provides
import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.component.ui.ticket.EventsRemoteRepository
import de.tum.`in`.tumcampusapp.component.ui.tufilm.repository.KinoLocalRepository
import de.tum.`in`.tumcampusapp.database.TcaDb

/**
 * This module provides dependencies needed in the kino functionality, for instance the data
 * repositories.
 */
@Module
class KinoModule {

    @Provides
    fun provideKinoLocalRepository(
            database: TcaDb
    ): KinoLocalRepository = KinoLocalRepository(database)

    @Provides
    fun provideEventsRemoteRepository(
            tumCabeClient: TUMCabeClient
    ): EventsRemoteRepository = EventsRemoteRepository(tumCabeClient)

}
