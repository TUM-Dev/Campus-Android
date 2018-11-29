package de.tum.`in`.tumcampusapp.component.ui.transportation.widget

import android.util.SparseArray
import de.tum.`in`.tumcampusapp.component.ui.transportation.model.WidgetsTransport
import de.tum.`in`.tumcampusapp.component.ui.transportation.model.efa.WidgetDepartures
import de.tum.`in`.tumcampusapp.component.ui.transportation.repository.TransportLocalRepository
import javax.inject.Inject

class MVVWidgetController @Inject constructor(
        private val localRepository: TransportLocalRepository
) {

    fun addWidget(appWidgetId: Int, widgetDepartures: WidgetDepartures) {
        val widgetsTransport = WidgetsTransport().apply {
            id = appWidgetId
            station = widgetDepartures.station
            stationId = widgetDepartures.stationId
            location = widgetDepartures.useLocation
            reload = widgetDepartures.autoReload
        }
        localRepository.insertWidget(widgetsTransport)
        widgetDeparturesList.put(appWidgetId, widgetDepartures)
    }

    fun deleteWidget(widgetId: Int) {
        localRepository.deleteWidget(widgetId)
        widgetDeparturesList.remove(widgetId)
    }

    fun getWidget(widgetId: Int): WidgetDepartures {
        if (widgetDeparturesList.indexOfKey(widgetId) >= 0) {
            return widgetDeparturesList.get(widgetId)
        }

        val widgetDepartures = WidgetDepartures().apply {
            localRepository.getById(widgetId)?.let {
                station = it.station
                stationId = it.stationId
                useLocation = it.location
                autoReload = it.reload
            }
        }

        widgetDeparturesList.put(widgetId, widgetDepartures)
        return widgetDepartures
    }

    companion object {

        val widgetDeparturesList = SparseArray<WidgetDepartures>()

    }

}
