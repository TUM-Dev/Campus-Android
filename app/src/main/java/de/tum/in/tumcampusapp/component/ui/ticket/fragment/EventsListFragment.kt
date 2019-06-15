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
import de.tum.`in`.tumcampusapp.component.other.generic.adapter.EqualSpacingItemDecoration
import de.tum.`in`.tumcampusapp.component.ui.chat.model.ChatMember
import de.tum.`in`.tumcampusapp.component.ui.ticket.EventsViewModel
import de.tum.`in`.tumcampusapp.component.ui.ticket.EventsViewState
import de.tum.`in`.tumcampusapp.component.ui.ticket.adapter.EventsAdapter
import de.tum.`in`.tumcampusapp.component.ui.ticket.di.EventsModule
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.EventType
import de.tum.`in`.tumcampusapp.di.ViewModelFactory
import de.tum.`in`.tumcampusapp.di.injector
import de.tum.`in`.tumcampusapp.utils.Const.CHAT_MEMBER
import de.tum.`in`.tumcampusapp.utils.Utils
import de.tum.`in`.tumcampusapp.utils.observeNonNull
import kotlinx.android.synthetic.main.fragment_events_list.eventPlaceholder
import kotlinx.android.synthetic.main.fragment_events_list.eventsRecyclerView
import kotlinx.android.synthetic.main.fragment_events_list.eventsRefreshLayout
import kotlinx.android.synthetic.main.fragment_events_list.placeholderImage
import kotlinx.android.synthetic.main.fragment_events_list.placeholderTextView
import javax.inject.Inject
import javax.inject.Provider

class EventsListFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {

    private val eventType: EventType by lazy {
        arguments?.getSerializable(KEY_EVENT_TYPE) as EventType
    }

    @Inject
    lateinit var provider: Provider<EventsViewModel>

    private val viewModel: EventsViewModel by lazy {
        val factory = ViewModelFactory(provider)
        ViewModelProviders.of(this, factory).get(EventsViewModel::class.java)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        injector.eventsComponent()
                .eventsModule(EventsModule())
                .eventType(eventType)
                .build()
                .inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_events_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(view) {
        eventsRecyclerView.setHasFixedSize(true)
        eventsRecyclerView.layoutManager = LinearLayoutManager(context)
        eventsRecyclerView.itemAnimator = DefaultItemAnimator()
        eventsRecyclerView.adapter = EventsAdapter(context)

        val spacing = Math.round(resources.getDimension(R.dimen.material_card_view_padding))
        eventsRecyclerView.addItemDecoration(EqualSpacingItemDecoration(spacing))

        eventsRefreshLayout.setOnRefreshListener(this@EventsListFragment)
        eventsRefreshLayout.setColorSchemeResources(
                R.color.color_primary,
                R.color.tum_A100,
                R.color.tum_A200
        )
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.viewState.observeNonNull(viewLifecycleOwner, this::render)
    }

    private fun render(viewState: EventsViewState) {
        val isEmpty = viewState.events.isEmpty()
        eventsRecyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
        eventPlaceholder.visibility = if (isEmpty) View.VISIBLE else View.GONE

        eventsRefreshLayout.isRefreshing = false

        if (viewState.events.isNotEmpty()) {
            val adapter = eventsRecyclerView.adapter as EventsAdapter
            adapter.update(viewState.events)
        } else {
            placeholderTextView.setText(eventType.placeholderTextId)
            placeholderImage.setImageResource(eventType.placeholderImageId)
        }

        eventsRefreshLayout.isRefreshing = viewState.isLoading
        viewState.errorResId?.let {
            Utils.showToast(requireContext(), it)
        }
    }

    override fun onRefresh() {
        val isLoggedIn = Utils.getSetting(requireContext(), CHAT_MEMBER, ChatMember::class.java) != null
        viewModel.refreshEventsAndTickets(isLoggedIn)
    }

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
