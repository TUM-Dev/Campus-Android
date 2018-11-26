package de.tum.`in`.tumcampusapp.component.ui.transportation

import android.content.Context
import de.tum.`in`.tumcampusapp.api.tumonline.CacheControl
import de.tum.`in`.tumcampusapp.component.other.locations.TumLocationManager
import de.tum.`in`.tumcampusapp.component.ui.overview.card.Card
import de.tum.`in`.tumcampusapp.component.ui.overview.card.CardsProvider
import de.tum.`in`.tumcampusapp.component.ui.transportation.repository.TransportRemoteRepository
import de.tum.`in`.tumcampusapp.utils.NetUtils
import java.util.*
import javax.inject.Inject

class TransportCardsProvider @Inject constructor(
        private val context: Context,
        private val remoteRepository: TransportRemoteRepository,
        private val tumLocationManager: TumLocationManager
) : CardsProvider {

    override fun provideCards(cacheControl: CacheControl): List<Card> {
        val results = ArrayList<Card>()
        if (!NetUtils.isConnected(context)) {
            return emptyList()
        }

        // Get station for current campus
        val station = tumLocationManager.getStation() ?: return emptyList()

        val departures = remoteRepository.fetchDeparturesAtStation(station.id).blockingFirst()
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
