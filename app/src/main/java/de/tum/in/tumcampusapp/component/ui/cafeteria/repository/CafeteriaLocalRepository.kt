package de.tum.`in`.tumcampusapp.component.ui.cafeteria.repository

import de.tum.`in`.tumcampusapp.component.ui.cafeteria.controller.CafeteriaManager
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.Cafeteria
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.CafeteriaMenu
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.CafeteriaWithMenus
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.utils.sync.model.Sync
import io.reactivex.Flowable
import org.joda.time.DateTime
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.inject.Inject

class CafeteriaLocalRepository @Inject constructor(
        private val database: TcaDb
) {

    private val executor: Executor = Executors.newSingleThreadExecutor()

    fun getCafeteriaWithMenus(cafeteriaId: Int): CafeteriaWithMenus {
        return CafeteriaWithMenus(cafeteriaId).apply {
            name = getCafeteriaNameFromId(id)
            menuDates = getAllMenuDates()
            menus = getCafeteriaMenus(id, nextMenuDate)
        }
    }

    private fun getCafeteriaNameFromId(id: Int): String? = getCafeteria(id)?.name

    // Menu methods //

    fun getCafeteriaMenus(id: Int, date: DateTime): List<CafeteriaMenu> {
        return database.cafeteriaMenuDao().getCafeteriaMenus(id, date)
    }

    fun getAllMenuDates(): List<DateTime> = database.cafeteriaMenuDao().allDates


    // Canteen methods //

    fun getAllCafeterias(): Flowable<List<Cafeteria>> = database.cafeteriaDao().all

    fun getCafeteria(id: Int): Cafeteria? = database.cafeteriaDao().getById(id)

    fun addCafeterias(cafeterias: List<Cafeteria>) = executor.execute {
        database.cafeteriaDao().insert(cafeterias)
    }

    // Sync methods //

    fun getLastSync() = database.syncDao().getSyncSince(CafeteriaManager::class.java.name, TIME_TO_SYNC)

    fun updateLastSync() = database.syncDao().insert(Sync(CafeteriaManager::class.java.name, DateTime.now()))

    fun clear() = database.cafeteriaDao().removeCache()

    companion object {
        private const val TIME_TO_SYNC = 604800 // 1 week
    }

}
