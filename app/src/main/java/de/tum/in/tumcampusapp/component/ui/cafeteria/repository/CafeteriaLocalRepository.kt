package de.tum.`in`.tumcampusapp.component.ui.cafeteria.repository

import de.tum.`in`.tumcampusapp.component.ui.cafeteria.controller.CafeteriaManager
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.Cafeteria
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.CafeteriaMenu
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.CafeteriaWithMenus
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.utils.sync.model.Sync
import io.reactivex.Flowable
import org.jetbrains.anko.doAsync
import org.joda.time.DateTime

class CafeteriaLocalRepository(private val db: TcaDb) {

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
        return db.cafeteriaMenuDao().getTypeNameFromDbCard(id, date)
    }

    fun getAllMenuDates(): List<DateTime> = db.cafeteriaMenuDao().allDates


    // Canteen methods //

    fun getAllCafeterias(): Flowable<List<Cafeteria>> = db.cafeteriaDao().all

    fun getCafeteria(id: Int): Cafeteria? = db.cafeteriaDao().getById(id)

    fun addCafeterias(cafeterias: List<Cafeteria>) {
        doAsync {
            db.cafeteriaDao().insert(*cafeterias.toTypedArray())
        }
    }

    // Sync methods //

    fun getLastSync() = db.syncDao().getSyncSince(CafeteriaManager::class.java.name, TIME_TO_SYNC)

    fun updateLastSync() = db.syncDao().insert(Sync(CafeteriaManager::class.java.name, DateTime.now()))

    fun clear() = db.cafeteriaDao().removeCache()

    companion object {
        private const val TIME_TO_SYNC = 604800
    }

}
