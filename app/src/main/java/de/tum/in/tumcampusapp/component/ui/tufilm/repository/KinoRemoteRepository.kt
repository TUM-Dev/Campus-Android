package de.tum.`in`.tumcampusapp.component.ui.tufilm.repository

import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.component.ui.ticket.payload.TicketStatus
import de.tum.`in`.tumcampusapp.component.ui.tufilm.model.Kino
import io.reactivex.Flowable
import retrofit2.Callback
import javax.inject.Inject

class KinoRemoteRepository @Inject constructor(
        private val tumCabeClient: TUMCabeClient
) {

    fun getAllKinos(lastId: String): Flowable<List<Kino>> = tumCabeClient.getKinos(lastId)

    fun fetchAvailableTicketCount(eventId: Int, callback: Callback<List<TicketStatus>>) {
        tumCabeClient.fetchTicketStats(eventId, callback)
    }

}