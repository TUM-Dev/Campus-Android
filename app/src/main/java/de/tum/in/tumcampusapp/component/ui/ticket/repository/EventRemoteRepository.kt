package de.tum.`in`.tumcampusapp.component.ui.ticket.repository

import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.Event

object EventRemoteRepository {

    lateinit var tumCabeClient: TUMCabeClient

    fun getAllEvents(): List<Event> = tumCabeClient.getEvents()

}