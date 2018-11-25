package de.tum.`in`.tumcampusapp.component.ui.cafeteria.repository

import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.CafeteriaMenu
import de.tum.`in`.tumcampusapp.database.TcaDb
import javax.inject.Inject

class CafeteriaMenuLocalRepository @Inject constructor(
        private val database: TcaDb
) {

    fun store(menus: List<CafeteriaMenu>) {
        database.cafeteriaMenuDao().insert(*menus.toTypedArray())
    }

    fun clear() {
        database.cafeteriaMenuDao().removeCache()
    }

}
