package de.tum.`in`.tumcampusapp.component.ui.tufilm.di

import android.content.Context
import dagger.Module
import dagger.Provides
import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.component.ui.ticket.repository.EventsLocalRepository
import de.tum.`in`.tumcampusapp.component.ui.ticket.repository.EventsRemoteRepository
import de.tum.`in`.tumcampusapp.component.ui.ticket.repository.TicketsLocalRepository
import de.tum.`in`.tumcampusapp.component.ui.ticket.repository.TicketsRemoteRepository
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
    fun provideTicketsLocalRepository(
            database: TcaDb
    ): TicketsLocalRepository = TicketsLocalRepository(database)

    @Provides
    fun provideTicketsRemoteRepository(
            context: Context,
            tumCabeClient: TUMCabeClient,
            localRepository: TicketsLocalRepository
    ): TicketsRemoteRepository = TicketsRemoteRepository(context, tumCabeClient, localRepository)

    @Provides
    fun provideEventsRemoteRepository(
            context: Context,
            tumCabeClient: TUMCabeClient,
            eventsLocalRepository: EventsLocalRepository,
            ticketsLocalRepository: TicketsLocalRepository,
            ticketsRemoteRepository: TicketsRemoteRepository
    ): EventsRemoteRepository {
        return EventsRemoteRepository(
                context,
                tumCabeClient,
                eventsLocalRepository,
                ticketsLocalRepository,
                ticketsRemoteRepository
        )
    }

}
