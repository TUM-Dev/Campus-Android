package de.tum.`in`.tumcampusapp.component.ui.ticket.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.generic.adapter.EqualSpacingItemDecoration
import de.tum.`in`.tumcampusapp.component.ui.ticket.EventsController
import de.tum.`in`.tumcampusapp.component.ui.ticket.adapter.EventsAdapter
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.Event
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.EventType
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.Ticket
import de.tum.`in`.tumcampusapp.utils.Utils
import kotlinx.android.synthetic.main.events_fragment.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EventsFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {

    private lateinit var eventType: EventType
    private lateinit var eventsController: EventsController

    private val eventsCallback = object : Callback<List<Event>> {
        override fun onResponse(call: Call<List<Event>>, response: Response<List<Event>>) {
            val events = response.body() ?: return

            eventsController.replaceEvents(events)
            if (eventType === EventType.ALL) {
                showEvents(events)
            }

            eventsRefreshLayout.isRefreshing = false
        }

        override fun onFailure(call: Call<List<Event>>, t: Throwable) {
            Utils.log(t)
            Utils.showToast(context, R.string.error_something_wrong)
            eventsRefreshLayout.isRefreshing = false
        }
    }

    private val ticketsCallback = object : Callback<List<Ticket>> {
        override fun onResponse(call: Call<List<Ticket>>, response: Response<List<Ticket>>) {
            val tickets = response.body() ?: return

            eventsController.replaceTickets(tickets)
            if (eventType === EventType.BOOKED) {
                val bookedEvents = eventsController.bookedEvents
                showEvents(bookedEvents)
            }

            eventsRefreshLayout.isRefreshing = false
        }

        override fun onFailure(call: Call<List<Ticket>>, t: Throwable) {
            Utils.log(t)
            Utils.showToast(context, R.string.error_something_wrong)
            eventsRefreshLayout.isRefreshing = false
        }
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.events_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(view) {
            eventsRecyclerView.setHasFixedSize(true)
            eventsRecyclerView.layoutManager = LinearLayoutManager(context)

            val spacing = Math.round(resources.getDimension(R.dimen.material_card_view_padding))
            eventsRecyclerView.addItemDecoration(EqualSpacingItemDecoration(spacing))

            eventsRefreshLayout.setOnRefreshListener(this@EventsFragment)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Create the events controller here as the context is only created when the
        // fragment is attached to an activity
        eventsController = EventsController(context)

        arguments?.let { args ->
            val eventType = args.getSerializable(KEY_EVENT_TYPE) as EventType
            val events = loadEventsFromDatabase(eventType)
            showEvents(events)
        }
    }

    override fun onStart() {
        super.onStart()
        eventsRecyclerView.adapter?.notifyDataSetChanged()
    }

    private fun loadEventsFromDatabase(type: EventType): List<Event> {
        return if (type === EventType.ALL) {
            eventsController.events
        } else {
            eventsController.bookedEvents
        }
    }

    private fun showEvents(events: List<Event>) {
        val isEmpty = events.isEmpty()
        eventsRecyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
        placeholderTextView.visibility = if (isEmpty) View.VISIBLE else View.GONE

        if (events.isNotEmpty()) {
            val adapter = EventsAdapter(context, events.sorted())
            eventsRecyclerView.adapter = adapter
            adapter.notifyDataSetChanged()
        } else {
            placeholderTextView.setText(eventType.placeholderResId)
        }
    }

    override fun onRefresh() {
        eventsController.getEventsAndTicketsFromServer(eventsCallback, ticketsCallback)
    }

    companion object {

        private const val KEY_EVENT_TYPE = "type"

        fun newInstance(eventType: EventType): EventsFragment {
            return EventsFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(KEY_EVENT_TYPE, eventType)
                }
            }
        }
    }

}
