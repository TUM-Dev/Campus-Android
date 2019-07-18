package de.tum.`in`.tumcampusapp.component.ui.cafeteria.controller

import android.content.Context
import de.tum.`in`.tumcampusapp.api.cafeteria.CafeteriaAPIClient
import de.tum.`in`.tumcampusapp.api.tumonline.CacheControl
import de.tum.`in`.tumcampusapp.component.notifications.NotificationScheduler
import de.tum.`in`.tumcampusapp.component.notifications.persistence.NotificationType
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.CafeteriaMenuDao
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.CafeteriaNotificationSettings
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.FavoriteDishDao
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.CafeteriaMenu
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.CafeteriaResponse
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.utils.DateTimeUtils
import de.tum.`in`.tumcampusapp.utils.Utils
import org.joda.time.DateTime
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

/**
 * Cafeteria Menu Manager, handles database stuff, external imports
 */
class CafeteriaMenuManager

@Inject
constructor(private val context: Context) {
    private val menuDao: CafeteriaMenuDao
    private val favoriteDishDao: FavoriteDishDao

    init {
        val db = TcaDb.getInstance(context)
        menuDao = db.cafeteriaMenuDao()
        favoriteDishDao = db.favoriteDishDao()
    }

    /**
     * Download cafeteria menus from external interface (JSON)
     *
     * @param cacheControl BYPASS_CACHE to force download over normal sync period, else false
     */
    fun downloadMenus(cacheControl: CacheControl) {
        // Responses from the cafeteria API are cached for one day. If the download is forced,
        // we add a "no-cache" header to the request.
        CafeteriaAPIClient
                .getInstance(context)
                .getMenus(cacheControl)
                .enqueue(object : Callback<CafeteriaResponse> {
                    override fun onResponse(call: Call<CafeteriaResponse>,
                                            response: Response<CafeteriaResponse>) {
                        val cafeteriaResponse = response.body()
                        if (cafeteriaResponse != null) {
                            onDownloadSuccess(cafeteriaResponse)
                        }
                    }

                    override fun onFailure(call: Call<CafeteriaResponse>, t: Throwable) {
                        Utils.log(t)
                    }
                })
    }

    private fun onDownloadSuccess(response: CafeteriaResponse) {
        menuDao.removeCache()
        menuDao.insert(response.menus)
        menuDao.insert(response.sideDishes)

        scheduleNotificationAlarms()
    }

    fun scheduleNotificationAlarms() {
        val menuDates = menuDao.allDates
        val settings = CafeteriaNotificationSettings(context)

        val notificationTimes = menuDates.mapNotNull {
            settings.retrieveLocalTime(it)
        }.map {
            it.toDateTimeToday()
        }

        val scheduler = NotificationScheduler(context)
        scheduler.scheduleAlarms(NotificationType.CAFETERIA, notificationTimes)
    }

    /**
     * Returns all the favorite dishes that a particular mensa serves on the specified date.
     *
     * @param queriedMensaId The Cafeteria for which to return the favorite dishes served
     * @param date The date for which to return the favorite dishes served
     * @return the favourite dishes at the given date
     */
    fun getFavoriteDishesServed(queriedMensaId: Int, date: DateTime): List<CafeteriaMenu> {
        val dateString = DateTimeUtils.getDateString(date)

        val upcomingServings = favoriteDishDao.getFavouritedCafeteriaMenuOnDate(dateString)
        return upcomingServings.filter {
            it.cafeteriaId == queriedMensaId
        }
    }

}