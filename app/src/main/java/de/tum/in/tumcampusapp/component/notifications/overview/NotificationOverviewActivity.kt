package de.tum.`in`.tumcampusapp.component.notifications.overview

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.notifications.receivers.NotificationAlarmReceiver
import de.tum.`in`.tumcampusapp.component.other.generic.activity.BaseActivity
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.databinding.ActivityNotificationOverviewBinding
import de.tum.`in`.tumcampusapp.utils.Const

class NotificationOverviewActivity : BaseActivity(R.layout.activity_notification_overview) {

    private lateinit var binding: ActivityNotificationOverviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNotificationOverviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbarInformation.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        // get currently active notifications
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val activeNotifications = notificationManager.activeNotifications.asList()
        // get notifications stored in database
        val scheduledNotifications = TcaDb.getInstance(applicationContext).scheduledNotificationsDao().getAllScheduledNotifications()
        val alarms = TcaDb.getInstance(applicationContext).activeNotificationsDao().getAllAlarms()
        // get scheduled for AlarmManager per NotificationType by PendingIntent
        val typesList = (0..5)

        // TODO add alarm manager general scheduled

        // TODO add alarm manager next


        // TODO show settings of alarm manager?


        // TODO remove
        Toast.makeText(applicationContext, String.format("Number active: %d, Number scheduled: %d, Number alarms: %d",
                activeNotifications.size, scheduledNotifications.size, alarms.size), Toast.LENGTH_LONG).show()


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
            getAlarmIntent(type)?.let {
                notificationsList.add(NotificationItemForStickyList(it.toString(), getString(R.string.alarmintents_per_type)))
            }
        }

        binding.notificationsListView.adapter = NotificationsListAdapter(applicationContext, notificationsList)
    }

    private fun getAlarmIntent(type: Int): PendingIntent? {
        val intent = Intent(applicationContext, NotificationAlarmReceiver::class.java).apply {
            putExtra(Const.KEY_NOTIFICATION_TYPE_ID, type)
        }
        // FLAG_NO_CREATE to only show existing intents
        return PendingIntent.getBroadcast(applicationContext, type, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE)
    }
}