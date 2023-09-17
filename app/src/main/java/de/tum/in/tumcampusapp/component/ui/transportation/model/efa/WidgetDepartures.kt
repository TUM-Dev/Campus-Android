package de.tum.`in`.tumcampusapp.component.ui.transportation.model.efa

import android.content.Context
import de.tum.`in`.tumcampusapp.component.ui.transportation.TransportController
import de.tum.`in`.tumcampusapp.component.ui.transportation.widget.MVVWidget
import java.util.*

/**
 * Create new WidgetDepartures. It contains the widget settingsPrefix and can load the according departure list
 *
 * @param station The station name
 * @param stationId The station name
 * @param useLocation Whether this widgets station is determined by the current location
 * @param autoReload If widget should update automatically, otherwise a button-press is required
 */
class WidgetDepartures(
    station: String = "",
    stationId: String = "",
    var useLocation: Boolean = false,
    var autoReload: Boolean = false,
    var departures: MutableList<Departure> = ArrayList()
) {

    private var lastLoad: Long = 0

    /**
     * Sets a station title for this widget
     *
     * @param station The station name
     */
    // TODO implement nearest station (replace the stationId string with the calculated station)
    var station: String = station
        get() {
            if (this.useLocation) {
                // LocationManager -> getStation
                this.station = "use location"
            }
            return field
        }

    /**
     * Are the departure information older than two minutes (because of any connection problems)
     *
     * @return True if only offline data available
     */
    var isOffline = false
        private set

    var stationId: String = stationId
        set(stationId) {
            if (this.stationId != stationId) {
                this.departures.clear()
            }
            field = stationId
        }

    /**
     * Get the list of departures for this widget, download them if they are not cached
     *
     * @return The list of departures
     */
    fun getDepartures(context: Context, forceServerLoad: Boolean): List<Departure> {
        // download only id there is no data or the last loading is more than X min ago
        val shouldAutoReload = System.currentTimeMillis() - this.lastLoad > MVVWidget.DOWNLOAD_DELAY
        if (this.departures.isEmpty() || forceServerLoad || this.autoReload && shouldAutoReload) {
            val departures = TransportController.getDeparturesFromExternal(context, this.stationId).blockingFirst()
            if (departures.isEmpty()) {
                this.isOffline = true
            } else {
                this.departures = departures.toMutableList()
                this.lastLoad = System.currentTimeMillis()
                this.isOffline = false
            }
        }

        // remove Departures which have a negative countdown
        val iterator = this.departures.iterator()
        while (iterator.hasNext()) {
            val departure = iterator.next()
            if (departure.departureTime.isBeforeNow) {
                iterator.remove()
            }
        }
        return this.departures
    }
}
