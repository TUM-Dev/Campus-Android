package de.tum.`in`.tumcampusapp.database.repository

import de.tum.`in`.tumcampusapp.models.cafeteria.Cafeteria
import io.reactivex.Observable
import retrofit2.http.GET

interface NetAPI {

    @GET(CAFETERIA_URL)
    fun getCafeterias() : Observable<List<Cafeteria>>

    companion object {
        const val CAFETERIA_URL = "mensen/"
    }
}
