package de.tum.`in`.tumcampusapp.component.ui.cafeteria.controller

import android.content.Context
import de.tum.`in`.tumcampusapp.api.cafeteria.CafeteriaAPIClient
import de.tum.`in`.tumcampusapp.api.tumonline.CacheControl
import de.tum.`in`.tumcampusapp.component.notifications.NotificationScheduler
import de.tum.`in`.tumcampusapp.component.notifications.persistence.NotificationType
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.*
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.CafeteriaLocation
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.CafeteriaMenu
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.deserialization.CafeteriaResponse
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
    private val cafeteriaDao: CafeteriaDao
    private val favoriteDishDao: FavoriteDishDao

    init {
        val db = TcaDb.getInstance(context)
        menuDao = db.cafeteriaMenuDao()
        favoriteDishDao = db.favoriteDishDao()
        cafeteriaDao = db.cafeteriaDao()
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
                .getMenus(cacheControl, CafeteriaLocation.MENSA_GARCHING)
                // TODO Implement lazy/dynamic menu fetching
                //  Actually have to do this right away, as this is the only way of getting the cafeteriaId
                //  If I dont know the id with which this fetching is called, then I have no way of getting the id at all
                //      => breaks new DB
                .enqueue(object : Callback<CafeteriaResponse> {
                    override fun onResponse(
                        call: Call<CafeteriaResponse>,
                        response: Response<CafeteriaResponse>
                    ) {
                        val cafeteriaResponse = response.body()
                        if (cafeteriaResponse != null) {
                            onDownloadSuccess(cafeteriaResponse)
                        } else {
                             Utils.logWithTag(this.javaClass.name, "Error fetching cafeteria menus. 'cafeteriaResponse' was null.")
                        }
                    }

                    override fun onFailure(call: Call<CafeteriaResponse>, t: Throwable) {
                        Utils.log(t)
                    }
                })
    }

    private fun onDownloadSuccess(response: CafeteriaResponse) {
        menuDao.removeCache()

        val menusToInsert = EatAPIParser.parseCafeteriaMenuFrom(response, cafeteriaDao.getIdFrom(CafeteriaLocation.MENSA_GARCHING.toSlug()))
        menuDao.insert(menusToInsert)

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

        // TODO revert/ fix Favorite dish
        val upcomingServings = /*favoriteDishDao.getFavouritedCafeteriaMenuOnDate(dateString)*/ emptyList<CafeteriaMenu>()
        return upcomingServings.filter { cafeteria: CafeteriaMenu ->
            cafeteria.cafeteriaId == queriedMensaId
        }
    }
}