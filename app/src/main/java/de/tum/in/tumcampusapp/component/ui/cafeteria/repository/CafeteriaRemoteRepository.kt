package de.tum.`in`.tumcampusapp.component.ui.cafeteria.repository

import android.annotation.SuppressLint
import android.content.Context
import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.api.cafeteria.CafeteriaAPIClient
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.EatAPIParser
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.controller.CafeteriaMenuManager
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.Cafeteria
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.CafeteriaLocation
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.deserialization.CafeteriaResponse
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.utils.Utils
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.joda.time.DateTime
import javax.inject.Inject

class CafeteriaRemoteRepository @Inject constructor(
    private val tumCabeClient: TUMCabeClient?,
    private val localRepository: CafeteriaLocalRepository,
    private val db: TcaDb
) {

    /**
     * Downloads cafeterias and stores them in the local repository.
     *
     * First checks whether a sync is necessary
     * Then clears current cache
     * Insert new cafeterias
     * Lastly updates last sync
     *
     */
    @SuppressLint("CheckResult")
    fun fetchCafeterias(force: Boolean) {
        // TumCabeClient is just a leftover from the old implementation
        // Cannot be swapped out yet due to the backend not being updated yet for
        // the fetching of cafeterias
        //      => Weaken constraint in constructor to allow dynamic fetching of cafeteria menus
        if(tumCabeClient != null) {
            Observable.just(1)
                    .filter { localRepository.getLastSync() == null || force }
                    .subscribeOn(Schedulers.io())
                    .doOnNext { localRepository.clear() }
                    .flatMap { tumCabeClient.cafeterias }
                    .doAfterNext { localRepository.updateLastSync() }
                    .subscribe(localRepository::addCafeterias, Utils::log)
        }
    }

    fun downloadRemoteMenus(cafeteriaId: Int, date: DateTime, context: Context) {
        // Get cafeteriaLocation from its id
        val cafeteriaLocation = CafeteriaLocation.fromString(db.cafeteriaDao().getSlugFrom(cafeteriaId))

        Utils.logWithTag(TAG, "Attempting to download remote menus for cafeteriaLocation: '$cafeteriaLocation' on date: $date.")

        // Responses from the cafeteria API are cached for one day. If the download is forced,
        // we add a "no-cache" header to the request.
        val response = CafeteriaAPIClient
                .getInstance(context)
                .getMenus(cafeteriaLocation, date)
                .execute()

        if(response.isSuccessful) {
            val cafeteriaResponse = response.body()
            if (cafeteriaResponse != null) {
                onDownloadSuccess(cafeteriaResponse, cafeteriaLocation, context)
            } else {
                Utils.logWithTag(TAG, "Error fetching cafeteria menus. 'cafeteriaResponse' was null.")
            }
        }
    }

    private fun onDownloadSuccess(response: CafeteriaResponse, cafeteriaLocation: CafeteriaLocation, context: Context) {
        val cafeteriaMenuManager = CafeteriaMenuManager(context)

        val menusToInsert = EatAPIParser.parseCafeteriaMenuFrom(response, db.cafeteriaDao().getIdFrom(cafeteriaLocation.toSlug()), cafeteriaLocation)
        db.cafeteriaMenuDao().insert(menusToInsert)

        cafeteriaMenuManager.scheduleNotificationAlarms()
    }

    companion object {
        val TAG: String = this::class.java.name
    }
}
