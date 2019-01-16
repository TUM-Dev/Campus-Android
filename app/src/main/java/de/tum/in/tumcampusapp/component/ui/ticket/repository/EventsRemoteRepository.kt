package de.tum.`in`.tumcampusapp.component.ui.ticket.repository

import android.annotation.SuppressLint
import android.content.Context
import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.api.app.exception.NoPrivateKey
import de.tum.`in`.tumcampusapp.component.ui.chat.model.ChatMember
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.Event
import de.tum.`in`.tumcampusapp.utils.Const.CHAT_MEMBER
import de.tum.`in`.tumcampusapp.utils.Utils
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class EventsRemoteRepository @Inject constructor(
        private val context: Context,
        private val tumCabeClient: TUMCabeClient,
        private val eventsLocalRepository: EventsLocalRepository,
        private val ticketsLocalRepository: TicketsLocalRepository,
        private val ticketsRemoteRepository: TicketsRemoteRepository
) {

    fun fetchEventsAndTickets() {
        fetchAndStoreEvents()

        val isLoggedIn = Utils.getSetting(context, CHAT_MEMBER, ChatMember::class.java) != null
        if (isLoggedIn) {
            fetchAndStoreTickets()
        }
    }

    @SuppressLint("CheckResult")
    private fun fetchAndStoreEvents() {
        fetchEvents()
                .subscribeOn(Schedulers.io())
                .subscribe(eventsLocalRepository::storeEvents, Utils::log)
    }

    @SuppressLint("CheckResult")
    private fun fetchAndStoreTickets() {
        try {
            tumCabeClient.fetchTickets(context)
                    .subscribeOn(Schedulers.io())
                    .doOnNext { ticketsRemoteRepository.fetchTicketTypesForTickets(it) }
                    .subscribe(ticketsLocalRepository::storeTickets, Utils::log)
        } catch (e: NoPrivateKey) {
            Utils.log(e)
        }
    }

    fun fetchEvents(): Observable<List<Event>> {
        return tumCabeClient.fetchEvents()
    }

    fun fetchTicketStats(eventId: Int): Single<Int> {
        return tumCabeClient.fetchTicketStats(eventId)
                .map { it.sumBy { status -> status.availableTicketCount } }
    }

}
