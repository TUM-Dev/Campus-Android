package de.tum.`in`.tumcampusapp.component.ui.transportation.repository

import de.tum.`in`.tumcampusapp.component.other.general.model.Recent
import de.tum.`in`.tumcampusapp.component.ui.transportation.model.TransportFavorites
import de.tum.`in`.tumcampusapp.component.ui.transportation.model.WidgetsTransport
import de.tum.`in`.tumcampusapp.component.ui.transportation.model.efa.StationResult
import de.tum.`in`.tumcampusapp.database.TcaDb
import javax.inject.Inject

class TransportLocalRepository @Inject constructor(
        private val database: TcaDb
) {

    fun insertWidget(widgetDepartures: WidgetsTransport) {
        database.transportDao().replaceWidget(widgetDepartures)
    }

    fun deleteWidget(widgetId: Int) {
        database.transportDao().deleteWidget(widgetId)
    }

    fun getById(widgetId: Int): WidgetsTransport? = database.transportDao().getAllWithId(widgetId)

    /**
     * Check if the transport symbol is one of the user's favorites.
     *
     * @param symbol The transport symbol
     * @return True, if favorite
     */
    fun isFavorite(symbol: String) = database.transportDao().isFavorite(symbol)

    /**
     * Adds a transport symbol to the list of the user's favorites.
     *
     * @param symbol The transport symbol
     */
    fun addFavorite(symbol: String) = database.transportDao().addFavorite(TransportFavorites(symbol = symbol))

    /**
     * Delete a user's favorite transport symbol.
     *
     * @param symbol The transport symbol
     */
    fun deleteFavorite(symbol: String) = database.transportDao().deleteFavorite(symbol)

    fun getRecentStations(recents: Collection<Recent>): List<StationResult> {
        return recents.mapNotNull {
            StationResult.fromRecent(it)
        }
    }

}
