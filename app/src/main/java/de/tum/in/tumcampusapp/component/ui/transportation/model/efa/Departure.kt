package de.tum.`in`.tumcampusapp.component.ui.transportation.model.efa

import de.tum.`in`.tumcampusapp.component.ui.transportation.api.MvvDeparture
import org.joda.time.DateTime
import org.joda.time.Minutes

data class Departure(
        val servingLine: String = "",
        val direction: String = "",
        val symbol: String = "",
        val countDown: Int = -1,
        val departureTime: DateTime = DateTime()
) {

    /**
     * Calculates the countDown with the real departure time and the current time
     *
     * @return The calculated countDown in minutes
     */
    val calculatedCountDown: Int
        get() = Minutes.minutesBetween(DateTime.now(), departureTime).minutes

    val formattedDirection: String
        get() = direction
                .replace(",", ", ")
                .replace("\\s+".toRegex(), " ")

    companion object {

        fun create(mvvDeparture: MvvDeparture): Departure {
            return Departure(
                    mvvDeparture.servingLine.name,
                    mvvDeparture.servingLine.direction,
                    mvvDeparture.servingLine.symbol,
                    mvvDeparture.countdown,
                    mvvDeparture.dateTime
            )
        }

    }

}
