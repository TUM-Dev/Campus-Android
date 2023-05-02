package de.tum.`in`.tumcampusapp.component.ui.overview

import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.generic.fragment.BaseFragment
import de.tum.`in`.tumcampusapp.component.tumui.lectures.fragment.LecturesFragment
import de.tum.`in`.tumcampusapp.databinding.ActivityNotificationDebugViewBinding


class NotificationDebugViewFragment : BaseFragment<String>(
        R.layout.fragment_notification_debug_view,
        // TODO als String
        R.string.notification_debug_view) {

    private val binding by viewBinding(ActivityNotificationDebugViewBinding::bind)

    override val swipeRefreshLayout get() = binding.swipeRefreshLayout
    override val layoutAllErrorsBinding get() = binding.layoutAllErrors

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /*binding.lecturesListView.setOnItemClickListener { _, _, position, _ ->
            val item = binding.lecturesListView.getItemAtPosition(position) as Lecture
            val intent = Intent(requireContext(), LectureDetailsActivity::class.java)
            intent.putExtra(Lecture.STP_SP_NR, item.stp_sp_nr)
            startActivity(intent)
        }*/


        val notificationManager = activity!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


        Toast.makeText(context, notificationManager.activeNotifications.size.toString() + notificationManager.activeNotifications.toString(), Toast.LENGTH_LONG).show();

        // init recycler view
        binding.recyclerViewNotifications.layoutManager = LinearLayoutManager(context);
        binding.recyclerViewNotifications.adapter = NotificationsListAdapter(notificationManager.activeNotifications)

        // nur möglich wenn extra databse query schreiben
        //TcaDb.getInstance(context).activeNotificationsDao().addActiveAlarm()
    }

    override fun onRefresh() {
        // TODO
        //loadPersonalLectures(CacheControl.BYPASS_CACHE)
    }


    companion object {
        @JvmStatic fun newInstance() = LecturesFragment()
    }

}
