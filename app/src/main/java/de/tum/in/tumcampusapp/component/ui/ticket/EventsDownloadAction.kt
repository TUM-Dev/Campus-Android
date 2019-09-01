package de.tum.`in`.tumcampusapp.component.ui.ticket

import de.tum.`in`.tumcampusapp.api.tumonline.CacheControl
import de.tum.`in`.tumcampusapp.component.ui.ticket.repository.EventsRemoteRepository
import de.tum.`in`.tumcampusapp.service.DownloadWorker
import javax.inject.Inject

class EventsDownloadAction @Inject constructor(
    private val eventsRemoteRepository: EventsRemoteRepository
) : DownloadWorker.Action {

    override fun execute(cacheBehaviour: CacheControl) {
        eventsRemoteRepository.fetchEventsAndTickets()
    }
}
