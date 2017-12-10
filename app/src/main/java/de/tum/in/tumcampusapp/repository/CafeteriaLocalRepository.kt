package de.tum.`in`.tumcampusapp.repository

import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.models.cafeteria.Cafeteria
import io.reactivex.Flowable
import java.util.concurrent.Executor
import java.util.concurrent.Executors

object CafeteriaLocalRepository {

    lateinit var db: TcaDb

    private val executor: Executor = Executors.newSingleThreadExecutor()

    fun getAllCafeterias(): Flowable<List<Cafeteria>> = db.cafeteriaDao().all

    fun getCafeteria(id: Int): Flowable<Cafeteria> = db.cafeteriaDao().getById(id)

    fun addCafeteria(cafeteria: Cafeteria) = executor.execute { db.cafeteriaDao().insert(cafeteria) }

}

