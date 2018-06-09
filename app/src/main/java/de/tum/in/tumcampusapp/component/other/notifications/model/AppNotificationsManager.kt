package de.tum.`in`.tumcampusapp.component.other.notifications.model

import android.content.Context
import de.tum.`in`.tumcampusapp.api.tumonline.AccessTokenManager
import de.tum.`in`.tumcampusapp.component.other.notifications.ProvidesNotifications
import de.tum.`in`.tumcampusapp.component.tumui.calendar.CalendarController
import de.tum.`in`.tumcampusapp.component.tumui.tutionfees.TuitionFeeManager
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.controller.CafeteriaManager
import de.tum.`in`.tumcampusapp.component.ui.news.NewsController
import de.tum.`in`.tumcampusapp.component.ui.transportation.TransportController

object AppNotificationsManager {

    private fun getProviders(context: Context): List<ProvidesNotifications> {
        return ArrayList<ProvidesNotifications>().apply {
            val tokenManager = AccessTokenManager(context)
            if (tokenManager.hasValidAccessToken()) {
                add(CalendarController(context))
                add(TuitionFeeManager(context))
            }

            add(CafeteriaManager(context))
            add(NewsController(context))
            add(TransportController(context))
        }
    }

    fun getEnabledProviders(context: Context) =
            getProviders(context).filter { it.hasNotificationsEnabled() }

}