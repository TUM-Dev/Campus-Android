package de.tum.`in`.tumcampusapp.component.ui.cafeteria.repository

import de.tum.`in`.tumcampusapp.component.ui.cafeteria.controller.CafeteriaManager
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.Cafeteria
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.CafeteriaMenu
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.CafeteriaWithMenus
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.CafeteriaLocation
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

    fun getCafeteriaWithMenus(cafeteriaLocation: CafeteriaLocation): CafeteriaWithMenus {
        return CafeteriaWithMenus(cafeteriaLocation).apply {
            name = getCafeteriaNameFromId(cafeteriaLocation)
            menuDates = getAllMenuDates()
            menus = getCafeteriaMenus(cafeteriaLocation, nextMenuDate)
        }
    }

    // Menu methods //
    fun getCafeteriaMenus(cafeteriaLocation: CafeteriaLocation, date: DateTime): List<CafeteriaMenu> {
        return database.cafeteriaMenuDao().getCafeteriaMenus(cafeteriaLocation.toId(), date)
    }

    fun getAllMenuDates(): List<DateTime> = database.cafeteriaMenuDao().allDates

    // Canteen methods //
    private fun getCafeteriaNameFromId(cafeteriaLocation: CafeteriaLocation): String? = database.cafeteriaDao().getById(cafeteriaLocation.toId())?.name

    fun getAllCafeterias(): Flowable<List<Cafeteria>> = database.cafeteriaDao().all

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
