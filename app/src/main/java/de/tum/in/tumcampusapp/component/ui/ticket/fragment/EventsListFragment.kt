package de.tum.`in`.tumcampusapp.component.ui.ticket.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.ui.ticket.EventsViewModel
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.EventType
import javax.inject.Inject
import javax.inject.Provider

class EventsListFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {

    @Inject
    lateinit var provider: Provider<EventsViewModel>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_events_list, container, false)
    }

    override fun onRefresh() = Unit

    companion object {

        private const val KEY_EVENT_TYPE = "type"

        @JvmStatic
        fun newInstance(eventType: EventType): EventsListFragment {
            return EventsListFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(KEY_EVENT_TYPE, eventType)
                }
            }
        }
    }
}
