package de.tum.`in`.tumcampusapp.component.ui.ticket.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.util.*

/**
 * Event
 *
 * @param id      Event-ID
 * @param image   Image url e.g. http://www.tu-film.de/img/film/poster/Fack%20ju%20Ghte.jpg
 * @param title   Title
 * @param description Description
 * @param locality Locality
 * @param date    Date
 * @param link    Url, e.g. http://www.in.tum.de
 */
@Entity
data class Event(@PrimaryKey
                @SerializedName("event")
                var id: Int = 0,
                var image: String = "",
                var title: String = "",
                var description: String = "",
                var locality: String = "",
                var date: Date = Date(),
                var link:  String = "")