package de.tum.`in`.tumcampusapp.component.ui.transportation.repository

import de.tum.`in`.tumcampusapp.component.ui.transportation.api.MvvApiService
import de.tum.`in`.tumcampusapp.component.ui.transportation.api.MvvDepartureList
import de.tum.`in`.tumcampusapp.component.ui.transportation.model.efa.Departure
import de.tum.`in`.tumcampusapp.component.ui.transportation.model.efa.StationResult
import io.reactivex.Observable
import javax.inject.Inject

class TransportRemoteRepository @Inject constructor(
        private val mvvService: MvvApiService
) {

    fun fetchDeparturesAtStation(stationID: String): Observable<List<Departure>> {
        return mvvService
                .getDepartures(stationID)
                .onErrorReturn { MvvDepartureList(emptyList()) }
                .map { it.departures.orEmpty() }
                .map { it.map { mvvDeparture -> Departure.create(mvvDeparture) } }
                .map { it.sortedBy { departure -> departure.countDown } }
    }

    fun fetchStationsByPrefix(prefix: String): Observable<List<StationResult>> {
        return mvvService
                .getStations(prefix)
                .map { it.stations.sortedBy { station -> station.quality } }
    }

}
