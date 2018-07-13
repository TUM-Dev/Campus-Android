package de.tum.`in`.tumcampusapp.component.ui.transportation.model.efa

import android.content.Context
import android.content.Intent
import de.tum.`in`.tumcampusapp.component.ui.transportation.TransportationDetailsActivity
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
        get() = Minutes.minutesBetween(departureTime, DateTime.now()).minutes

    fun getIntent(context: Context, station: StationResult): Intent? =
            Intent(context, TransportationDetailsActivity::class.java).apply {
                putExtra(TransportationDetailsActivity.EXTRA_STATION, station.id)
                putExtra(TransportationDetailsActivity.EXTRA_STATION_ID, station.station)
            }
}