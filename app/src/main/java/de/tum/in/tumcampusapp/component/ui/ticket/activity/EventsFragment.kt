package de.tum.`in`.tumcampusapp.component.ui.ticket.activity

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.generic.fragment.FragmentForDownloadingExternal
import de.tum.`in`.tumcampusapp.component.ui.ticket.EventsDownloadAction
import de.tum.`in`.tumcampusapp.component.ui.ticket.fragment.EventsListFragment
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.EventType
import de.tum.`in`.tumcampusapp.di.injector
import de.tum.`in`.tumcampusapp.service.DownloadWorker
import kotlinx.android.synthetic.main.fragment_events.viewPager
import java.util.Arrays
import javax.inject.Inject

class EventsFragment : FragmentForDownloadingExternal(
    R.layout.fragment_events,
    R.string.events_tickets
) {

    @Inject
    lateinit var eventsDownloadAction: EventsDownloadAction

    override val method: DownloadWorker.Action?
        get() = eventsDownloadAction

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        injector.downloadComponent().inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewPager(viewPager)

        val eventTab = requireActivity().findViewById<TabLayout>(R.id.event_tab)
        eventTab.setupWithViewPager(viewPager)

        eventTab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPager.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) = Unit

            override fun onTabReselected(tab: TabLayout.Tab) = Unit
        })
    }

    private fun setupViewPager(viewPager: ViewPager) {
        val adapter = EventsViewPagerAdapter(requireContext(), requireFragmentManager())
        viewPager.adapter = adapter
    }

    /**
     * This class manages two tabs.
     * One of them shows all available events, the other one all booked events.
     */
    internal class EventsViewPagerAdapter(
        context: Context,
        manager: FragmentManager
    ) : FragmentPagerAdapter(manager) {

        private val titles = Arrays.asList(
            context.getString(R.string.all_events),
            context.getString(R.string.booked_events)
        )

        override fun getItem(position: Int): Fragment {
            val type = if (position == 0) EventType.ALL else EventType.BOOKED
            return EventsListFragment.newInstance(type)
        }

        override fun getCount(): Int {
            return titles.size
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return titles[position]
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = EventsFragment()
    }
}