package de.tum.`in`.tumcampusapp.component.ui.transportation.model.efa

import java.util.concurrent.TimeUnit

data class Departure(var servingLine: String = "",
                     var direction: String = "",
                     var symbol: String = "",
                     var countDown: Int = -1,
                     var departureTime: Long = -1) {

    /**
     * Calculates the countDown with the real departure time and the current time
     *
     * @return The calculated countDown in minutes
     */
    val calculatedCountDown: Long
        get() = TimeUnit.MINUTES.convert(departureTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS)
}