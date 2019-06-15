package de.tum.`in`.tumcampusapp.component.ui.ticket.di

import dagger.Binds
import dagger.Module
import de.tum.`in`.tumcampusapp.component.ui.ticket.EventsDownloadAction
import de.tum.`in`.tumcampusapp.service.DownloadWorker

@Module
interface TicketsModule {

    @Binds
    fun bindEventsDownloadAction(impl: EventsDownloadAction): DownloadWorker.Action

}
