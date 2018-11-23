package de.tum.`in`.tumcampusapp.component.ui.cafeteria.controller

import android.content.Context
import android.location.Location
import de.tum.`in`.tumcampusapp.component.notifications.ProvidesNotifications
import de.tum.`in`.tumcampusapp.component.other.locations.LocationManager
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.CafeteriaDao
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.Cafeteria
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.CafeteriaMenu
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.CafeteriaWithMenus
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.repository.CafeteriaLocalRepository
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.utils.LocationHelper.calculateDistanceToCafeteria
import de.tum.`in`.tumcampusapp.utils.Utils
import org.jetbrains.anko.defaultSharedPreferences

class CafeteriaManager(
        private val context: Context,
        private val locationManager: LocationManager,
        private val localRepository: CafeteriaLocalRepository
) : ProvidesNotifications {

    private val cafeteriaDao: CafeteriaDao by lazy {
        TcaDb.getInstance(context).cafeteriaDao()
    }

    fun getBestMatchCafeteriaMenus(): List<CafeteriaMenu> {
        val cafeteriaId = getBestMatchMensaId()
        return if (cafeteriaId == -1) {
            emptyList()
        } else getCafeteriaMenusByCafeteriaId(cafeteriaId)
    }

    fun getBestMatchMensaId(): Int {
        // Choose which mensa should be shown
        val location = locationManager.getCurrentOrNextLocation()
        val campus = locationManager.getCurrentOrNextCampus()
        val cafeteriaId = getClosestCafeteriaId(location, campus)
        if (cafeteriaId == -1) {
            Utils.log("could not get a Cafeteria from locationManager!")
        }
        return cafeteriaId
    }

    private fun getCafeteriaMenusByCafeteriaId(cafeteriaId: Int): List<CafeteriaMenu> {
        val cafeteria = CafeteriaWithMenus(cafeteriaId)

        val menuDates = localRepository.getAllMenuDates()
        cafeteria.menuDates = menuDates

        val nextMenuDate = cafeteria.nextMenuDate
        val menus = localRepository.getCafeteriaMenus(cafeteriaId, nextMenuDate)
        cafeteria.menus = menus

        return cafeteria.menus
    }

    override fun hasNotificationsEnabled(): Boolean {
        return Utils.getSettingBool(context, "card_cafeteria_phone", true)
    }

    fun getClosestCafeteriaId(location: Location, campus: LocationManager.Companion.Campus?): Int {
        campus?.let {
            return getDefaultCampusCafeteriaId(it)
        }

        val allCafeterias = getAllCafeterias(location)
        return if (allCafeterias.isEmpty()) -1 else allCafeterias[0].id
    }

    private fun getAllCafeterias(location: Location): List<Cafeteria> {
        return cafeteriaDao.allNow
                .map { it.copy(distance = calculateDistanceToCafeteria(it, location)) }
                .sorted()
    }

    private fun getDefaultCampusCafeteriaId(campus: LocationManager.Companion.Campus): Int {
        val prefs = context.defaultSharedPreferences
        val cafeteria = prefs.getString("card_cafeteria_default_" + campus.short, campus.defaultMensa)
        return cafeteria.toInt()
    }

}
