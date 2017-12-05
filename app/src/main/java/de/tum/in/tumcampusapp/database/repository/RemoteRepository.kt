package de.tum.`in`.tumcampusapp.database.repository

import de.tum.`in`.tumcampusapp.models.cafeteria.Cafeteria
import io.reactivex.Observable

interface RemoteRepository {

    fun getAllCafeterias() : Observable<List<Cafeteria>>

}