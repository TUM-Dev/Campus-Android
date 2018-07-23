package de.tum.`in`.tumcampusapp.component.notifications.model

import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.api.tumonline.AccessTokenManager
import de.tum.`in`.tumcampusapp.component.notifications.ProvidesNotifications
import de.tum.`in`.tumcampusapp.component.tumui.calendar.CalendarController
import de.tum.`in`.tumcampusapp.component.tumui.tutionfees.TuitionFeeManager
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.controller.CafeteriaManager
import de.tum.`in`.tumcampusapp.component.ui.news.NewsController
import de.tum.`in`.tumcampusapp.component.ui.transportation.TransportController
import de.tum.`in`.tumcampusapp.utils.Const
import org.jetbrains.anko.notificationManager

object AppNotificationsManager {

    private fun getProviders(context: Context): List<ProvidesNotifications> {
        return ArrayList<ProvidesNotifications>().apply {
            if (AccessTokenManager.hasValidAccessToken(context)) {
                add(CalendarController(context))
                add(TuitionFeeManager(context))
            }

            add(CafeteriaManager(context))
            add(NewsController(context))
            add(TransportController(context))
        }
    }

    fun getEnabledProviders(context: Context): List<ProvidesNotifications> {
        return getProviders(context).filter { it.hasNotificationsEnabled() }
    }

    @TargetApi(Build.VERSION_CODES.O)
    @JvmStatic
    fun setupNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val default = createChannel(
                context, Const.NOTIFICATION_CHANNEL_DEFAULT,
                R.string.channel_general, R.string.channel_description_general,
                NotificationManager.IMPORTANCE_DEFAULT
        )

        val chat = createChannel(
                context, Const.NOTIFICATION_CHANNEL_CHAT,
                R.string.channel_chat, R.string.channel_description_chat,
                NotificationManager.IMPORTANCE_DEFAULT
        )

        val eduroam = createChannel(
                context, Const.NOTIFICATION_CHANNEL_EDUROAM,
                R.string.eduroam, R.string.channel_description_eduroam,
                NotificationManager.IMPORTANCE_LOW
        )

        val cafeteria = createChannel(
                context, Const.NOTIFICATION_CHANNEL_CAFETERIA,
                R.string.channel_cafeteria, R.string.channel_description_cafeteria,
                NotificationManager.IMPORTANCE_LOW
        )

        val mvv = createChannel(
                context, Const.NOTIFICATION_CHANNEL_MVV,
                R.string.channel_mvv, R.string.channel_description_mvv,
                NotificationManager.IMPORTANCE_LOW
        )

        val emergency = createChannel(
                context, Const.NOTIFICATION_CHANNEL_EMERGENCY,
                R.string.channel_tum_alarmierung, R.string.channel_description_tum_alarmierung,
                NotificationManager.IMPORTANCE_HIGH).apply {
            enableLights(true)
            enableVibration(true)
            lightColor = Color.RED
        }

        val notificationManager = context.notificationManager
        val channels = listOf(default, chat, eduroam, cafeteria, mvv, emergency)

        channels.forEach { notificationManager.createNotificationChannel(it) }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun createChannel(context: Context, id: String, nameResId: Int,
                              descriptionResId: Int, importance: Int): NotificationChannel {
        return NotificationChannel(id, context.getString(nameResId), importance).apply {
            description = context.getString(descriptionResId)
        }
    }

}

