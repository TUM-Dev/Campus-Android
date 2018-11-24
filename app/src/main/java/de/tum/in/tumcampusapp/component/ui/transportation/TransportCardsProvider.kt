package de.tum.`in`.tumcampusapp.component.ui.transportation

import android.content.Context
import de.tum.`in`.tumcampusapp.api.tumonline.CacheControl
import de.tum.`in`.tumcampusapp.component.other.locations.TumLocationManager
import de.tum.`in`.tumcampusapp.component.ui.overview.card.Card
import de.tum.`in`.tumcampusapp.component.ui.overview.card.CardsProvider
import de.tum.`in`.tumcampusapp.utils.NetUtils
import java.util.*
import javax.inject.Inject

class TransportCardsProvider @Inject constructor(
        private val context: Context,
        private val transportController: TransportController
) : CardsProvider {

    override fun provideCards(cacheControl: CacheControl): List<Card> {
        val results = ArrayList<Card>()
        if (!NetUtils.isConnected(context)) {
            return emptyList()
        }

        // Get station for current campus
        val locMan = TumLocationManager(context)
        val station = locMan.getStation() ?: return emptyList()

        val departures = transportController.fetchDeparturesAtStation(station.id).blockingFirst()
        val card = MVVCard(context).apply {
            setStation(station)
            setDepartures(departures)
        }

        card.getIfShowOnStart()?.let {
            results.add(it)
        }

        return results
    }

}
