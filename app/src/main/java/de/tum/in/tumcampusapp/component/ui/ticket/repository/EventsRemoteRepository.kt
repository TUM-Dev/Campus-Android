package de.tum.`in`.tumcampusapp.component.ui.ticket.repository

import android.annotation.SuppressLint
import android.content.Context
import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.component.ui.chat.model.ChatMember
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.Event
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.Ticket
import de.tum.`in`.tumcampusapp.component.ui.ticket.payload.TicketStatus
import de.tum.`in`.tumcampusapp.utils.Const.CHAT_MEMBER
import de.tum.`in`.tumcampusapp.utils.Utils
import io.reactivex.Completable
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
        val tickets = ticketsRemoteRepository.fetchTickets().share()

        tickets.flatMapCompletable { storeTickets(it) }
                .onErrorComplete()
                .subscribe()

        tickets.flatMapCompletable { ticketsRemoteRepository.fetchTicketTypesForTickets(it) }
                .onErrorComplete()
                .subscribe()
    }

    private fun storeTickets(tickets: List<Ticket>): Completable {
        return Completable.fromCallable {
            ticketsLocalRepository.storeTickets(tickets)
            Completable.complete()
        }
    }

    fun fetchEvents(): Observable<List<Event>> {
        return tumCabeClient.fetchEvents()
    }

    fun fetchTicketStats(eventId: Int): Single<TicketStatus> {
        return tumCabeClient.fetchTicketStats(eventId)
                .map { it.reduceRight { s, a -> TicketStatus(-1, a.contingent + s.contingent, a.sold + s.sold) } }
    }
}
