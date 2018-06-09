package de.tum.`in`.tumcampusapp.component.other.notifications.model

import android.content.Context
import de.tum.`in`.tumcampusapp.component.other.notifications.ProvidesNotifications
import de.tum.`in`.tumcampusapp.component.tumui.calendar.CalendarController
import de.tum.`in`.tumcampusapp.component.tumui.tutionfees.TuitionFeeManager
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.controller.CafeteriaManager
import de.tum.`in`.tumcampusapp.component.ui.news.NewsController
import de.tum.`in`.tumcampusapp.component.ui.transportation.TransportController

object AppNotificationsManager {

    fun getProviders(context: Context) = arrayOf<ProvidesNotifications>(
            CafeteriaManager(context),
            CalendarController(context),
            NewsController(context),
            TransportController(context),
            TuitionFeeManager(context)
    )

}