package de.tum.`in`.tumcampusapp.component.ui.cafeteria

import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.notifications.NotificationProvider
import de.tum.`in`.tumcampusapp.component.notifications.model.AppNotification
import de.tum.`in`.tumcampusapp.component.notifications.model.InstantNotification
import de.tum.`in`.tumcampusapp.component.notifications.persistence.NotificationType
import de.tum.`in`.tumcampusapp.component.other.locations.TumLocationManager
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.controller.CafeteriaManager
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.controller.CafeteriaMenuManager
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.MenuType
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.repository.CafeteriaLocalRepository
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.DateTimeUtils
import org.joda.time.DateTime
import javax.inject.Inject

class CafeteriaNotificationProvider @Inject constructor(
        context: Context,
        private val locationManager: TumLocationManager,
        private val cafeteriaManager: CafeteriaManager,
        private val cafeteriaMenuManager: CafeteriaMenuManager,
        private val localRepository: CafeteriaLocalRepository
) : NotificationProvider(context) {

    override fun getNotificationBuilder(): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, Const.NOTIFICATION_CHANNEL_CAFETERIA)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_cutlery)
                .setGroup(GROUP_KEY_CAFETERIA)
                .setGroupSummary(true)
                .setShowWhen(false)
                .setColor(notificationColorAccent)
    }

    override fun buildNotification(): AppNotification? {
        val location = locationManager.getCurrentOrNextLocation()
        val campus = locationManager.getCurrentOrNextCampus()
        val cafeteriaId = cafeteriaManager.getClosestCafeteriaId(location, campus)
        if (cafeteriaId == -1) {
            return null
        }

        val cafeteria = localRepository.getCafeteriaWithMenus(cafeteriaId)
        val menus = cafeteria.menus.filter { it.menuType != MenuType.SIDE_DISH }
        val intent = cafeteria.getIntent(context)

        val inboxStyle = NotificationCompat.InboxStyle()
        menus.forEach { inboxStyle.addLine(it.notificationTitle) }

        val title = context.getString(R.string.cafeteria)

        val favoriteDishes = cafeteriaMenuManager.getFavoriteDishesServed(cafeteriaId, DateTime.now())

        // If any of the user's favorite dishes are served, we include them in the notification
        // text. Otherwise, we simply put the day's date.
        val text = if (favoriteDishes.isNotEmpty()) {
            val dishes = favoriteDishes.joinToString(", ") { it.name }
            context.getString(R.string.including_format_string, dishes)
        } else {
            DateTimeUtils.getDateString(cafeteria.nextMenuDate)
        }

        val pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val summaryNotification = getNotificationBuilder()
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(inboxStyle)
                .setContentIntent(pendingIntent)
                .build()

        // We can pass 0 as the notification ID because only one notification at a time
        // will be active
        return InstantNotification(NotificationType.CAFETERIA, cafeteria.id, summaryNotification)
    }

    companion object {
        private const val GROUP_KEY_CAFETERIA = "de.tum.in.tumcampus.CAFETERIA"
    }

}
