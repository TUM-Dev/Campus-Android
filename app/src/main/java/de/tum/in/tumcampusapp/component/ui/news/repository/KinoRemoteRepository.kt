package de.tum.`in`.tumcampusapp.component.ui.news.repository

import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.component.ui.tufilm.model.Kino
import io.reactivex.Observable

object KinoRemoteRepository {

    lateinit var tumCabeClient: TUMCabeClient

    fun getAllKinos(lastId: String): Observable<List<Kino>> = tumCabeClient.getKinos(lastId)

}