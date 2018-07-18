package de.tum.`in`.tumcampusapp.component.ui.transportation.model.efa

import org.joda.time.DateTime
import org.joda.time.Minutes

data class Departure(var servingLine: String = "",
                     var direction: String = "",
                     var symbol: String = "",
                     var countDown: Int = -1,
                     var departureTime: DateTime = DateTime()) {

    /**
     * Calculates the countDown with the real departure time and the current time
     *
     * @return The calculated countDown in minutes
     */
    val calculatedCountDown: Int
        get() = Minutes.minutesBetween(DateTime.now(), departureTime).minutes
}