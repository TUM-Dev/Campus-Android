package de.tum.`in`.tumcampusapp.component.notifications.model

import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.support.annotation.RequiresApi
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.api.tumonline.AccessTokenManager
import de.tum.`in`.tumcampusapp.component.notifications.ProvidesNotifications
import de.tum.`in`.tumcampusapp.component.tumui.calendar.CalendarController
import de.tum.`in`.tumcampusapp.component.tumui.tutionfees.TuitionFeeManager
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.controller.CafeteriaManager
import de.tum.`in`.tumcampusapp.component.ui.news.NewsController
import de.tum.`in`.tumcampusapp.component.ui.transportation.TransportController
import de.tum.`in`.tumcampusapp.utils.Const

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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun Context.buildNotificationChannel(id: String, nameRes: Int, importance: Int) =
            NotificationChannel(id, getString(nameRes), importance)

    /**
     * Set up NotificationChannels for Android O
     */
    @TargetApi(Build.VERSION_CODES.O)
    fun setupNotificationChannels(c: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            (c.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?)?.apply {
                listOf(
                        c.buildNotificationChannel(Const.NOTIFICATION_CHANNEL_DEFAULT, R.string.channel_general, NotificationManager.IMPORTANCE_DEFAULT),
                        c.buildNotificationChannel(Const.NOTIFICATION_CHANNEL_CHAT, R.string.channel_chat, NotificationManager.IMPORTANCE_DEFAULT)
                                .apply { enableLights(true) },
                        c.buildNotificationChannel(Const.NOTIFICATION_CHANNEL_EDUROAM, R.string.eduroam, NotificationManager.IMPORTANCE_LOW),
                        c.buildNotificationChannel(Const.NOTIFICATION_CHANNEL_CAFETERIA, R.string.channel_cafeteria, NotificationManager.IMPORTANCE_LOW),
                        c.buildNotificationChannel(Const.NOTIFICATION_CHANNEL_MVV, R.string.channel_mvv, NotificationManager.IMPORTANCE_LOW),
                        c.buildNotificationChannel(Const.NOTIFICATION_CHANNEL_EMERGENCY, R.string.channel_tum_alarmierung, NotificationManager.IMPORTANCE_HIGH)
                                .apply {
                                    enableVibration(true)
                                    enableLights(true)
                                    lightColor = Color.RED
                                }
                ).forEach { createNotificationChannel(it) }
            }
        }
    }

}