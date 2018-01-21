package de.tum.`in`.tumcampusapp.repository

import de.tum.`in`.tumcampusapp.api.TUMCabeClient
import de.tum.`in`.tumcampusapp.models.cafeteria.Cafeteria
import de.tum.`in`.tumcampusapp.models.tumcabe.Kino
import io.reactivex.Observable

object KinoRemoteRepository {

    lateinit var tumCabeClient: TUMCabeClient

    fun getAllKinos(lastId: String): Observable<List<Kino>> = tumCabeClient.getKinos(lastId)

}