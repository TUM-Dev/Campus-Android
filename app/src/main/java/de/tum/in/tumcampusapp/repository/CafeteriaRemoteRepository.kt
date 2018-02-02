package de.tum.`in`.tumcampusapp.repository

import de.tum.`in`.tumcampusapp.api.TUMCabeClient
import de.tum.`in`.tumcampusapp.models.cafeteria.Cafeteria
import io.reactivex.Observable

object CafeteriaRemoteRepository {

    lateinit var tumCabeClient: TUMCabeClient

    fun getAllCafeterias(): Observable<List<Cafeteria>> = tumCabeClient.cafeterias

}
