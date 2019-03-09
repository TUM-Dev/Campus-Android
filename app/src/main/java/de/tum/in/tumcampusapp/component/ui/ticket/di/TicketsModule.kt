package de.tum.`in`.tumcampusapp.component.ui.ticket.di

import android.content.Context
import dagger.Module
import dagger.Provides
import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.component.ui.ticket.EventsDownloadAction
import de.tum.`in`.tumcampusapp.component.ui.ticket.repository.EventsLocalRepository
import de.tum.`in`.tumcampusapp.component.ui.ticket.repository.EventsRemoteRepository
import de.tum.`in`.tumcampusapp.component.ui.ticket.repository.TicketsLocalRepository
import de.tum.`in`.tumcampusapp.component.ui.ticket.repository.TicketsRemoteRepository
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.service.DownloadWorker

@Module
class TicketsModule {

    @Provides
    fun provideTicketsLocalRepository(
            database: TcaDb
    ): TicketsLocalRepository {
        return TicketsLocalRepository(database)
    }

    @Provides
    fun provideEventsRemoteRepository(
            context: Context,
            tumCabeClient: TUMCabeClient,
            eventsLocalRepository: EventsLocalRepository,
            ticketsLocalRepository: TicketsLocalRepository,
            ticketsRemoteRepository: TicketsRemoteRepository
    ): EventsRemoteRepository {
        return EventsRemoteRepository(context, tumCabeClient,
                eventsLocalRepository, ticketsLocalRepository, ticketsRemoteRepository)
    }

    @Provides
    fun provideEventsDownloadAction(
            remoteRepository: EventsRemoteRepository
    ): DownloadWorker.Action = EventsDownloadAction(remoteRepository)

}
