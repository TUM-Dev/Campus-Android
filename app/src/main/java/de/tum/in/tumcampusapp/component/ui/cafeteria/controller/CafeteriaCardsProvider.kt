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
import javax.inject.Inject

class CafeteriaCardsProvider @Inject constructor(
        private val context: Context,
        private val cafeteriaManager: CafeteriaManager,
        private val localRepository: CafeteriaLocalRepository,
        private val locationManager: TumLocationManager
) : ProvidesCard {

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
        val cafeteriaId = cafeteriaManager.getClosestCafeteriaId(location, campus)
        return if (cafeteriaId == -1) {
            null
        } else localRepository.getCafeteriaWithMenus(cafeteriaId)
    }

}
