package de.tum.`in`.tumcampusapp.component.ui.ticket.model

import de.tum.`in`.tumcampusapp.R

enum class EventType(val placeholderTextId: Int, val placeholderImageId: Int) {
    ALL(R.string.no_events_found, R.drawable.popcorn_placeholder),
    BOOKED(R.string.no_bookings_found, R.drawable.tickets_placeholder)
}