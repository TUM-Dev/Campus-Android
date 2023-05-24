package de.tum.`in`.tumcampusapp.component.notifications.overview

import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.generic.activity.BaseActivity
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.databinding.ActivityNotificationOverviewBinding

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

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager




        // lists of notifications retrieved from different sources
        val activeNotifications = notificationManager.activeNotifications.asList()
        val scheduledNotifications = TcaDb.getInstance(applicationContext).scheduledNotificationsDao().getAllScheduledNotifications()
        val alarms = TcaDb.getInstance(applicationContext).activeNotificationsDao().getAllAlarms()

        // TODO remove
        Toast.makeText(applicationContext, String.format("Number active: %d, Number scheduled: %d, Number alarms: %d",
                activeNotifications.size, scheduledNotifications.size, alarms.size), Toast.LENGTH_LONG).show()



        val notificationsList = emptyList<NotificationItemForStickyList>().toMutableList()


        activeNotifications.forEach {
            notificationsList.add(NotificationItemForStickyList(it.toString(), "Active Notifications"))
        }

        scheduledNotifications.forEach {
            notificationsList.add(NotificationItemForStickyList(it.toString(), "Scheduled Notifications"))
        }

        alarms.forEach {
            notificationsList.add(NotificationItemForStickyList(it.toString(), "Alarms"))
        }


        binding.notificationsListView.adapter = NotificationsListAdapter(applicationContext, notificationsList)
    }


}