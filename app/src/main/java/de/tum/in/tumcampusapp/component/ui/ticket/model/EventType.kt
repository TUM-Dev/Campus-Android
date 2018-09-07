package de.tum.`in`.tumcampusapp.component.ui.ticket.model

import de.tum.`in`.tumcampusapp.R

enum class EventType(val placeholderResId: Int) {
    ALL(R.string.no_events_found),
    BOOKED(R.string.no_bookings_found)
}