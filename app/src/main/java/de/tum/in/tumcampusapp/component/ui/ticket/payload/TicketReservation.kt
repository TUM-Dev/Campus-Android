package de.tum.`in`.tumcampusapp.component.ui.ticket.payload

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey


@Entity
data class TicketReservation(@PrimaryKey
                      var ticket_type: Int = 0)