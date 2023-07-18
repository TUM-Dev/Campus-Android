package de.tum.`in`.tumcampusapp.component.notifications.overview

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.notifications.receivers.NotificationAlarmReceiver
import de.tum.`in`.tumcampusapp.component.notifications.receivers.NotificationReceiver
import de.tum.`in`.tumcampusapp.component.other.generic.activity.BaseActivity
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.databinding.ActivityNotificationOverviewBinding

class NotificationOverviewActivity : BaseActivity(R.layout.activity_notification_overview) {

    private lateinit var binding: ActivityNotificationOverviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNotificationOverviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbarNotificationOverview.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            setTitle(R.string.notification_overview)
        }

        // get currently active notifications
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val activeNotifications = notificationManager.activeNotifications.asList()
        // get notifications stored in database
        val scheduledNotifications = TcaDb.getInstance(applicationContext).scheduledNotificationsDao().getAllScheduledNotifications()
        val alarms = TcaDb.getInstance(applicationContext).activeNotificationsDao().getAllAlarms()
        // get PendingIntent for every NotificationType
        val typesList = (0..5)
        // get PendingIntent for every ScheduledNotification
        val scheduledNotificationIds = scheduledNotifications.map { it.typeId }

        // store all notifications into one list
        val notificationsList = emptyList<NotificationItemForStickyList>().toMutableList()
        activeNotifications.forEach {
            notificationsList.add(NotificationItemForStickyList(it.toString(), getString(R.string.active_notifications)))
        }
        scheduledNotifications.forEach {
            notificationsList.add(NotificationItemForStickyList(it.toString(), getString(R.string.scheduled_notifications)))
        }
        alarms.forEach {
            notificationsList.add(NotificationItemForStickyList(it.toString(), getString(R.string.alarms)))
        }
        typesList.forEach { type ->
            getAlarmIntentPerType(type)?.let {
                notificationsList.add(NotificationItemForStickyList(it.toString(), getString(R.string.pendingintents_per_type)))
            }
        }
        scheduledNotificationIds.forEach { id ->
            getAlarmIntent(id)?.let {
                notificationsList.add(NotificationItemForStickyList(it.toString(), getString(R.string.pendingintents_per_scheduled_notification)))
            }
        }

        binding.notificationsListView.adapter = NotificationsListAdapter(applicationContext, notificationsList)
    }

    // mimics NotificationScheduler.getAlarmIntent(type: NotificationType) method
    private fun getAlarmIntentPerType(type: Int): PendingIntent? {
        // extra data is ignored according to intent filter
        val intent = Intent(applicationContext, NotificationAlarmReceiver::class.java)
        // FLAG_NO_CREATE to only show existing intents
        return PendingIntent.getBroadcast(applicationContext, type, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE)
    }

    // mimics NotificationScheduler.getAlarmIntent(futureNotification: FutureNotification, globalNotificationId: Long) method
    private fun getAlarmIntent(id: Int): PendingIntent? {
        // extra data is ignored according to intent filter
        val intent = Intent(applicationContext, NotificationReceiver::class.java)
        // FLAG_NO_CREATE to only show existing intents
        return PendingIntent.getBroadcast(
            applicationContext,
            id,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
        )
    }
}
