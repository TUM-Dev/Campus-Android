package de.tum.`in`.tumcampusapp.models.efa

import java.util.concurrent.TimeUnit

data class Departure(val servingLine: String, val direction: String, val symbol: String, val countDown: Int, val departureTime: Long) {

    /**
     * Calculates the countDown with the real departure time and the current time
     *
     * @return The calculated countDown in minutes
     */
    val calculatedCountDown: Long
        get() = TimeUnit.MINUTES.convert(departureTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS)
}