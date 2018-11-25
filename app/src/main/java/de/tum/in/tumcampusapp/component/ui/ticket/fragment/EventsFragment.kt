package de.tum.`in`.tumcampusapp.component.ui.ticket.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.generic.activity.BaseActivity
import de.tum.`in`.tumcampusapp.component.other.generic.adapter.EqualSpacingItemDecoration
import de.tum.`in`.tumcampusapp.component.ui.chat.model.ChatMember
import de.tum.`in`.tumcampusapp.component.ui.ticket.EventsViewModel
import de.tum.`in`.tumcampusapp.component.ui.ticket.adapter.EventsAdapter
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.Event
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.EventType
import de.tum.`in`.tumcampusapp.di.ViewModelFactory
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Utils
import de.tum.`in`.tumcampusapp.utils.observe
import de.tum.`in`.tumcampusapp.utils.observeNonNull
import kotlinx.android.synthetic.main.fragment_events.*
import javax.inject.Inject
import javax.inject.Provider


class EventsFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {

    private lateinit var eventType: EventType

    @Inject
    lateinit var provider: Provider<EventsViewModel>

    private val viewModel: EventsViewModel by lazy {
        val factory = ViewModelFactory(provider)
        ViewModelProviders.of(this, factory).get(EventsViewModel::class.java)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        (requireActivity() as BaseActivity).injector.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_events, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(view) {
        eventsRecyclerView.setHasFixedSize(true)
        eventsRecyclerView.layoutManager = LinearLayoutManager(context)
        eventsRecyclerView.itemAnimator = DefaultItemAnimator()
        eventsRecyclerView.adapter = EventsAdapter(context)

        val spacing = Math.round(resources.getDimension(R.dimen.material_card_view_padding))
        eventsRecyclerView.addItemDecoration(EqualSpacingItemDecoration(spacing))

        eventsRefreshLayout.setOnRefreshListener(this@EventsFragment)
        eventsRefreshLayout.setColorSchemeResources(
                R.color.color_primary,
                R.color.tum_A100,
                R.color.tum_A200
        )
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        eventType = arguments?.getSerializable(KEY_EVENT_TYPE) as EventType

        viewModel.getEvents(eventType).observeNonNull(viewLifecycleOwner, this::showEvents)
        viewModel.error.observe(viewLifecycleOwner, this::showError)
    }

    private fun showEvents(events: List<Event>) {
        val isEmpty = events.isEmpty()
        eventsRecyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
        placeholderTextView.visibility = if (isEmpty) View.VISIBLE else View.GONE

        eventsRefreshLayout.isRefreshing = false

        if (events.isNotEmpty()) {
            val adapter = eventsRecyclerView.adapter as EventsAdapter
            adapter.update(events)
        } else {
            placeholderTextView.setText(eventType.placeholderResId)
        }
    }

    private fun showError(errorMessageResId: Int?) {
        eventsRefreshLayout.isRefreshing = false
        errorMessageResId?.let {
            Utils.showToast(requireContext(), it)
        }
    }

    override fun onRefresh() {
        val isLoggedIn = Utils.getSetting(requireContext(),
                Const.CHAT_MEMBER, ChatMember::class.java) != null
        viewModel.fetchEventsAndTickets(isLoggedIn)
    }

    companion object {

        private const val KEY_EVENT_TYPE = "type"

        @JvmStatic
        fun newInstance(eventType: EventType): EventsFragment {
            return EventsFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(KEY_EVENT_TYPE, eventType)
                }
            }
        }
    }

}
