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
    private val favoriteDishDao: FavoriteDishDao

    init {
        val db = TcaDb.getInstance(context)
        menuDao = db.cafeteriaMenuDao()
        favoriteDishDao = db.favoriteDishDao()
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