package de.tum.`in`.tumcampusapp.component.ui.ticket.fragment

import androidx.lifecycle.ViewModel
import de.tum.`in`.tumcampusapp.component.ui.ticket.di.EventId
import javax.inject.Inject

class EventDetailsViewModel @Inject constructor(
    @EventId val eventId: Int
) : ViewModel()
