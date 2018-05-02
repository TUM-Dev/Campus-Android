package de.tum.`in`.tumcampusapp.service

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.CafeteriaNotificationSettings
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.activity.CafeteriaActivity
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.controller.CafeteriaMenuManager
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.CafeteriaMenu
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.DateUtils
import de.tum.`in`.tumcampusapp.utils.Utils
import java.util.*

/**
 * AlarmNotification class for scheduling future favorite food notification.
 * To support backward compatibility, one notification is constructed per
 * found dish. This also ensures that tapping it shows the user the correct
 * cafeteria in the newly opened cafeteria activity. The alarm itself,
 * will launch at a given day and then consult the FavoriteFoodAlarmStorage's scheduledEntries
 * to find out whether there are still outstanding notifications at that specific day, or
 * if they've been canceled in the meantime. Depending on the result, the notification will
 * either be triggered or the alarm will do nothing.
 */
class FavoriteDishAlarmScheduler : BroadcastReceiver() {

    fun setFoodAlarm(context: Context, dateString: String) {
        val scheduledAt = loadTriggerHourAndMinute(context, dateString) ?: return
        val today = Calendar.getInstance()
        if (today.after(scheduledAt)) {
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val schedule = constructAlarmIntent(context, dateString)
        alarmManager.setWindow(AlarmManager.RTC_WAKEUP, scheduledAt.timeInMillis, 1000, schedule)
    }

    fun cancelFoodAlarm(context: Context, dateString: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(constructAlarmIntent(context, dateString))
    }

    /**
     * Generates a pending intent for a future alarm at a given date
     */
    private fun constructAlarmIntent(context: Context, dateString: String): PendingIntent {
        val intent = Intent(context, FavoriteDishAlarmScheduler::class.java).apply {
            putExtra("triggeredAt", dateString)
            action = IDENTIFIER_STRING
        }
        return PendingIntent.getBroadcast(context,
                dateString.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    /**
     * Can either receive a date or a boolean cancelNotifications value. This way other activities
     * can close the currently opened notifications and it is possible to schedule dates, where the
     * alarm has to check for favorite dishes.
     *
     * @param context
     * @param extra   Extra can either be "cancelNotifications" or a date, when the alarm should check, if there are any
     * favorite dishes at a given date.
     */
    override fun onReceive(context: Context, extra: Intent) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        cancelFoodNotifications(notificationManager)
        if (extra.getBooleanExtra("cancelNotifications", false)) {
            return
        }

        val triggeredAt = extra.getStringExtra("triggeredAt")

        val scheduledNow = CafeteriaMenuManager(context)
                .getServedFavoritesAtDate(triggeredAt)
        if (scheduledNow == null) {
            Utils.log("FavoriteDishAlarmScheduler: Scheduled now is null, onReceived aborted")
            return
        }

        val dao = TcaDb
                .getInstance(context)
                .cafeteriaDao()

        scheduledNow.keys.forEach { mensaId ->
            val menuCount = scheduledNow[mensaId]?.size ?: 0
            val scheduledForMensaID = scheduledNow[mensaId] ?: emptySet<CafeteriaMenu>()
            val message = scheduledForMensaID
                    .map { menu -> menu.name }
                    .joinToString("\n")

            ACTIVE_NOTIFICATIONS.add(mensaId)
            val mensaName = dao.getMensaNameFromId(mensaId)

            val intent = Intent(context, CafeteriaActivity::class.java).apply {
                putExtra(Const.MENSA_FOR_FAVORITEDISH, mensaId)
            }

            val pendingIntent = PendingIntent.getActivity(context, mensaId, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            val notification = NotificationCompat.Builder(context, Const.NOTIFICATION_CHANNEL_CAFETERIA)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(mensaName + if (menuCount > 1) " ($menuCount)" else "")
                    .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                    .setContentText(message)
                    .setAutoCancel(true)
                    .setLargeIcon(Utils.getLargeIcon(context, R.drawable.ic_cutlery))
                    .setContentIntent(pendingIntent)
                    .setDefaults(Notification.DEFAULT_SOUND)
                    .setAutoCancel(true)
                    .build()
            notificationManager.notify(IDENTIFIER_STRING, mensaId, notification)
        }
    }

    private fun cancelFoodNotifications(notificationManager: NotificationManager) {
        synchronized(ACTIVE_NOTIFICATIONS) {
            val it = ACTIVE_NOTIFICATIONS.iterator()
            while (it.hasNext()) {
                notificationManager.cancel(IDENTIFIER_STRING, it.next())
                it.remove()
            }
        }
    }

    /**
     * Checks if the user set or disabled (hour = -1) an hour for a potential schedule.
     */
    private fun loadTriggerHourAndMinute(context: Context, dateString: String): Calendar? {
        val scheduledAt = Calendar.getInstance().apply {
            time = DateUtils.getDate(dateString)
        }
        val hourMinute = CafeteriaNotificationSettings(context).retrieveHourMinute(scheduledAt)
        hourMinute.first?.let { hour ->
            if (hour != -1) {
                scheduledAt.set(Calendar.HOUR_OF_DAY, hour)
            }
        }
        hourMinute.second?.let { minute -> scheduledAt.set(Calendar.MINUTE, minute) }
        return scheduledAt
    }

    companion object {
        private val ACTIVE_NOTIFICATIONS = Collections.synchronizedSet(HashSet<Int>())
        private const val IDENTIFIER_STRING = "TCA_FAV_FOOD"
    }

}
