package de.tum.`in`.tumcampusapp.component.ui.cafeteria.controller

import android.content.Context
import de.tum.`in`.tumcampusapp.api.tumonline.CacheControl
import de.tum.`in`.tumcampusapp.component.other.locations.TumLocationManager
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.CafeteriaMenuCard
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.CafeteriaWithMenus
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.repository.CafeteriaLocalRepository
import de.tum.`in`.tumcampusapp.component.ui.overview.card.Card
import de.tum.`in`.tumcampusapp.component.ui.overview.card.ProvidesCard
import java.util.*

class CafeteriaCardsProvider(
        private val context: Context,
        private val localRepository: CafeteriaLocalRepository
) : ProvidesCard {

    private val locationManager = TumLocationManager(context)
    private val cafeteriaManager2 = CafeteriaManager(context, locationManager, localRepository)

    override fun getCards(cacheControl: CacheControl): List<Card> {
        val results = ArrayList<Card>()

        val cafeteria = getCafeteriaWithMenus() ?: return results

        val card = CafeteriaMenuCard(context)
        card.setCafeteriaWithMenus(cafeteria)

        card.getIfShowOnStart()?.let {
            results.add(it)
        }

        return results
    }

    private fun getCafeteriaWithMenus(): CafeteriaWithMenus? {
        // Choose which mensa should be shown
        //val cafeteriaId = LocationManager(context).getCafeteria()
        val location = locationManager.getCurrentOrNextLocation()
        val campus = locationManager.getCurrentOrNextCampus()
        val cafeteriaId = cafeteriaManager2.getClosestCafeteriaId(location, campus)
        return if (cafeteriaId == -1) {
            null
        } else localRepository.getCafeteriaWithMenus(cafeteriaId)
    }

}
