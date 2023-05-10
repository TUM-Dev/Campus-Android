package de.tum.`in`.tumcampusapp.component.ui.overview

import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.generic.fragment.BaseFragment
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.databinding.FragmentNotificationDebugViewBinding


class NotificationDebugViewFragment : BaseFragment<String>(
        R.layout.fragment_notification_debug_view,
        R.string.notification_debug_view) {

    private val binding by viewBinding(FragmentNotificationDebugViewBinding::bind)

    override val swipeRefreshLayout get() = binding.swipeRefreshLayout
    override val layoutAllErrorsBinding get() = binding.layoutAllErrors

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val notificationManager = activity!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // lists of notifications retrieved from different sources
        val activeNotifications = notificationManager.activeNotifications.asList()
        val scheduledNotifications = TcaDb.getInstance(context!!).scheduledNotificationsDao().getAllScheduledNotifications()
        val alarms = TcaDb.getInstance(context!!).activeNotificationsDao().getAllAlarms()





        Toast.makeText(context, String.format("Number active: %d, Number scheduled: %d, Number alarms: %d",
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


        binding.notificationsListView.adapter = NotificationsListAdapter(context!!, notificationsList)

    }

    override fun onRefresh() {
        // TODO
        //loadPersonalLectures(CacheControl.BYPASS_CACHE)
    }


    companion object {
        @JvmStatic fun newInstance() = NotificationDebugViewFragment()
    }

}
