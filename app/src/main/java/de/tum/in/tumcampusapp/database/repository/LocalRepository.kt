package de.tum.`in`.tumcampusapp.database.repository

import de.tum.`in`.tumcampusapp.models.cafeteria.Cafeteria
import io.reactivex.Flowable


interface LocalRepository {
    fun getAllCafeterias() : Flowable<List<Cafeteria>>
    fun getCafeteria(id:Int) : Flowable<Cafeteria>
    fun addCafeteria(cafeteria: Cafeteria)
}