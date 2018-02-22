package de.tum.`in`.tumcampusapp.component.ui.cafeteria.repository

import de.tum.`in`.tumcampusapp.component.ui.cafeteria.controller.CafeteriaManager
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.Cafeteria
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.CafeteriaMenu
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.utils.DateUtils
import de.tum.`in`.tumcampusapp.utils.sync.model.Sync
import io.reactivex.Flowable
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors

object CafeteriaLocalRepository {

    private const val TIME_TO_SYNC = 604800

    private val executor: Executor = Executors.newSingleThreadExecutor()

    lateinit var db: TcaDb


    // Menu methods //

    fun getCafeteriaMenu(id: Int, date: String): Flowable<List<CafeteriaMenu>> =
            db.cafeteriaMenuDao().getTypeNameFromDbCard(id, date)

    fun getAllMenuDates(): Flowable<List<String>> = db.cafeteriaMenuDao().allDates


    // Canteen methods //

    fun getAllCafeterias(): Flowable<List<Cafeteria>> = db.cafeteriaDao().all

    fun getCafeteria(id: Int): Flowable<Cafeteria> = db.cafeteriaDao().getById(id)

    fun addCafeteria(cafeteria: Cafeteria) = executor.execute { db.cafeteriaDao().insert(cafeteria) }


    // Sync methods //

    fun getLastSync() = db.syncDao().getSyncSince(CafeteriaManager::class.java.name, TIME_TO_SYNC)

    fun updateLastSync() = db.syncDao().insert(Sync(CafeteriaManager::class.java.name, DateUtils.getDateTimeString(Date())))

    fun clear() = db.cafeteriaDao().removeCache()

}


