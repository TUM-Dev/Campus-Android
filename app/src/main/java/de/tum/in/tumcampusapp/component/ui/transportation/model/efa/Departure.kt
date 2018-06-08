package de.tum.`in`.tumcampusapp.component.ui.transportation.model.efa

import android.content.Context
import android.content.Intent
import de.tum.`in`.tumcampusapp.component.ui.transportation.TransportationDetailsActivity
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

    fun getIntent(context: Context, station: StationResult): Intent? =
            Intent(context, TransportationDetailsActivity::class.java).apply {
                putExtra(TransportationDetailsActivity.EXTRA_STATION, station.id)
                putExtra(TransportationDetailsActivity.EXTRA_STATION_ID, station.station)
            }

}