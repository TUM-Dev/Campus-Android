package de.tum.`in`.tumcampusapp.component.ui.transportation

import android.content.Context
import android.util.SparseArray
import de.tum.`in`.tumcampusapp.api.tumonline.CacheControl
import de.tum.`in`.tumcampusapp.component.notifications.NotificationScheduler
import de.tum.`in`.tumcampusapp.component.notifications.ProvidesNotifications
import de.tum.`in`.tumcampusapp.component.other.general.model.Recent
import de.tum.`in`.tumcampusapp.component.other.locations.LocationManager
import de.tum.`in`.tumcampusapp.component.tumui.calendar.model.Event
import de.tum.`in`.tumcampusapp.component.ui.overview.card.Card
import de.tum.`in`.tumcampusapp.component.ui.overview.card.ProvidesCard
import de.tum.`in`.tumcampusapp.component.ui.transportation.api.MvvClient
import de.tum.`in`.tumcampusapp.component.ui.transportation.api.MvvDepartureList
import de.tum.`in`.tumcampusapp.component.ui.transportation.model.TransportFavorites
import de.tum.`in`.tumcampusapp.component.ui.transportation.model.WidgetsTransport
import de.tum.`in`.tumcampusapp.component.ui.transportation.model.efa.Departure
import de.tum.`in`.tumcampusapp.component.ui.transportation.model.efa.StationResult
import de.tum.`in`.tumcampusapp.component.ui.transportation.model.efa.WidgetDepartures
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.utils.NetUtils
import de.tum.`in`.tumcampusapp.utils.Utils
import io.reactivex.Observable
import java.util.*

/**
 * Transport Manager, handles querying data from mvv and card creation
 */
class TransportController(private val context: Context) : ProvidesCard, ProvidesNotifications {

    private val transportDao = TcaDb.getInstance(context).transportDao()

    /**
     * Check if the transport symbol is one of the user's favorites.
     *
     * @param symbol The transport symbol
     * @return True, if favorite
     */
    fun isFavorite(symbol: String) = transportDao.isFavorite(symbol)

    /**
     * Adds a transport symbol to the list of the user's favorites.
     *
     * @param symbol The transport symbol
     */
    fun addFavorite(symbol: String) = transportDao.addFavorite(TransportFavorites(symbol = symbol))

    /**
     * Delete a user's favorite transport symbol.
     *
     * @param symbol The transport symbol
     */
    fun deleteFavorite(symbol: String) = transportDao.deleteFavorite(symbol)

    /**
     * Adds the settingsPrefix of a widget to the widget list, replaces the existing settingsPrefix
     * if there are some
     */
    fun addWidget(appWidgetId: Int, widgetDepartures: WidgetDepartures) {
        val widgetsTransport = WidgetsTransport().apply {
            id = appWidgetId
            station = widgetDepartures.station
            stationId = widgetDepartures.stationId
            location = widgetDepartures.useLocation
            reload = widgetDepartures.autoReload
        }
        transportDao.replaceWidget(widgetsTransport)
        widgetDeparturesList.put(appWidgetId, widgetDepartures)
    }

    /**
     * Deletes the settingsPrefix of a widget to the widget list
     *
     * @param widgetId The id of the widget
     */
    fun deleteWidget(widgetId: Int) {
        transportDao.deleteWidget(widgetId)
        widgetDeparturesList.remove(widgetId)
    }

    /**
     * A WidgetDepartures Object containing the settingsPrefix of this widget.
     * This object can provide the departures needed by this widget as well.
     * The settingsPrefix are cached, only the first time its loded from the database.
     * If there is no widget with this id saved (in cache and the database) a new WidgetDepartures
     * Object is generated containing a NULL for the station and an empty string for the station id.
     * This is not cached or saved to the database.
     *
     * @param widgetId The id of the widget
     * @return The WidgetDepartures Object
     */
    fun getWidget(widgetId: Int): WidgetDepartures {
        if (widgetDeparturesList.indexOfKey(widgetId) >= 0) {
            return widgetDeparturesList.get(widgetId)
        }
        val widgetDepartures = WidgetDepartures().apply {
            transportDao.getAllWithId(widgetId)?.let {
                station = it.station
                stationId = it.stationId
                useLocation = it.location
                autoReload = it.reload
            }
        }

        widgetDeparturesList.put(widgetId, widgetDepartures)
        return widgetDepartures
    }

    override fun getCards(cacheControl: CacheControl): List<Card> {
        val results = ArrayList<Card>()
        if (!NetUtils.isConnected(context)) {
            return emptyList()
        }

        // Get station for current campus
        val locMan = LocationManager(context)
        val station = locMan.getStation() ?: return emptyList()

        val departures = getDeparturesFromExternal(context, station.id).blockingFirst()
        val card = MVVCard(context).apply {
            setStation(station)
            setDepartures(departures)
        }

        card.getIfShowOnStart()?.let {
            results.add(it)
        }

        return results
    }

    override fun hasNotificationsEnabled(): Boolean {
        return Utils.getSettingBool(context, "card_mvv_phone", false)
    }

    fun scheduleNotifications(events: List<Event>) {
        if (events.isEmpty()) {
            return
        }

        // Schedule a notification alarm for every last calendar item of a day
        val notificationCandidates = events
                .dropLast(1)
                .filterIndexed { index, current ->
                    val next = events[index + 1]
                    if (current.startTime == null || next.startTime == null) {
                        false
                    } else {
                        current.startTime.dayOfYear != next.startTime.dayOfYear
                    }
                }
                .take(100) // Some manufacturers cap the amount of alarms you can schedule (https://stackoverflow.com/a/29610474)

        val notifications = notificationCandidates.mapNotNull { it.toNotification(context) }
        NotificationScheduler(context).schedule(notifications)
    }

    companion object {
        private var widgetDeparturesList = SparseArray<WidgetDepartures>()

        /**
         * Get all departures for a station.
         *
         * @param stationID Station ID, station name might or might not work
         * @return List of departures
         */
        @JvmStatic
        fun getDeparturesFromExternal(context: Context, stationID: String): Observable<List<Departure>> {
            return MvvClient.getInstance(context)
                    .getDepartures(stationID)
                    .onErrorReturn { MvvDepartureList(emptyList()) }
                    .map { it.departures.orEmpty() }
                    .map { it.map { mvvDeparture -> Departure.create(mvvDeparture) } }
                    .map { it.sortedBy { departure -> departure.countDown } }
        }

        /**
         * Find stations by station name prefix
         *
         * @param prefix Name prefix
         * @return List of StationResult
         */
        @JvmStatic
        fun getStationsFromExternal(context: Context, prefix: String): Observable<List<StationResult>> {
            return MvvClient.getInstance(context)
                    .getStations(prefix)
                    .map { it.stations.sortedBy { station -> station.quality } }
        }

        @JvmStatic
        fun getRecentStations(recents: Collection<Recent>): List<StationResult> {
            return recents.mapNotNull {
                StationResult.fromRecent(it)
            }
        }
    }
}
