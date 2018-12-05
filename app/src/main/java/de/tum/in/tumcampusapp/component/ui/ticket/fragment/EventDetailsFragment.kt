package de.tum.`in`.tumcampusapp.component.ui.ticket.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.squareup.picasso.Picasso
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.component.tumui.calendar.CreateEventActivity
import de.tum.`in`.tumcampusapp.component.ui.ticket.EventHelper
import de.tum.`in`.tumcampusapp.component.ui.ticket.EventsController
import de.tum.`in`.tumcampusapp.component.ui.ticket.EventsRemoteRepository
import de.tum.`in`.tumcampusapp.component.ui.ticket.activity.ShowTicketActivity
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.Event
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Const.KEY_EVENT_ID
import de.tum.`in`.tumcampusapp.utils.DateTimeUtils
import de.tum.`in`.tumcampusapp.utils.into
import de.tum.`in`.tumcampusapp.utils.observe
import kotlinx.android.synthetic.main.fragment_event_details.*
import kotlinx.android.synthetic.main.fragment_event_details.view.*

/**
 * Fragment for displaying information about an [Event]. Manages content that's shown in the
 * PagerAdapter.
 */
class EventDetailsFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {

    private val event: Event by lazy {
        arguments?.getParcelable(Const.KEY_EVENT)
                ?: throw IllegalStateException("No event provided to EventDetailsFragment")
    }

    private val eventsController: EventsController by lazy {
        EventsController(requireContext())
    }

    private val viewModel: EventDetailsViewModel by lazy {
        val remoteRepo = EventsRemoteRepository(TUMCabeClient.getInstance(requireContext()))
        val factory = EventDetailsViewModel.Factory(event.id, remoteRepo)
        ViewModelProviders.of(this, factory).get(EventDetailsViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = LayoutInflater.from(container?.context)
                .inflate(R.layout.fragment_event_details, container, false)

        view.swipeRefreshLayout.setOnRefreshListener(this)
        view.swipeRefreshLayout.setColorSchemeResources(
                R.color.color_primary,
                R.color.tum_A100,
                R.color.tum_A200
        )

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        showEventDetails(event)
        viewModel.ticketCount.observe(viewLifecycleOwner, this::showTicketCount)
    }

    override fun onRefresh() {
        viewModel.fetchTicketCount()
    }

    override fun onResume() {
        super.onResume()
        if (!eventsController.isEventBooked(event) && EventHelper.isEventImminent(event)) {
            ticketButton.visibility = View.GONE
        }
    }

    private fun showEventDetails(event: Event) {
        val url = event.imageUrl
        if (url != null) {
            Picasso.get()
                    .load(url)
                    .noPlaceholder()
                    .into(posterView) {
                        posterProgressBar?.visibility = View.GONE
                    }
        } else {
            posterProgressBar.visibility = View.GONE
        }

        if (eventsController.isEventBooked(event)) {
            ticketButton.text = getString(R.string.show_ticket)
            ticketButton.setOnClickListener { showTicket(event) }
        } else {
            ticketButton.text = getString(R.string.buy_ticket)
            ticketButton.setOnClickListener { EventHelper.buyTicket(event, ticketButton, context) }
        }

        context?.let {
            dateTextView.text = event.getFormattedStartDateTime(it)
            dateContainer.setOnClickListener { _ -> displayAddToCalendarDialog() }
        }

        locationTextView.text = event.locality
        locationContainer.setOnClickListener { openMaps(event) }

        descriptionTextView.text = event.description

        linkButton.setOnClickListener { openEventLink(event) }
        linkButton.visibility = if (event.eventUrl.isNotBlank()) View.VISIBLE else View.GONE
    }

    private fun openEventLink(event: Event) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(event.eventUrl))
        startActivity(intent)
    }

    private fun showTicketCount(count: Int?) {
        val text = count?.toString() ?: getString(R.string.unknown)
        remainingTicketsTextView.text = text
        swipeRefreshLayout.isRefreshing = false
    }

    private fun showTicket(event: Event) {
        val intent = Intent(context, ShowTicketActivity::class.java).apply {
            putExtra(KEY_EVENT_ID, event.id)
        }
        startActivity(intent)
    }

    private fun addToTUMCalendar() {
        val event = event ?: return
        val endTime = event.endTime ?: event.startTime.plus(Event.defaultDuration.toLong())

        val intent = Intent(context, CreateEventActivity::class.java).apply {
            putExtra(Const.EVENT_EDIT, false)
            putExtra(Const.EVENT_TITLE, event.title)
            putExtra(Const.EVENT_COMMENT, event.description)
            putExtra(Const.EVENT_START, event.startTime)
            putExtra(Const.EVENT_END, endTime)
        }

        startActivity(intent)
    }

    private fun addToExternalCalendar() {
        val event = event ?: return
        val endTime = event.endTime ?: event.startTime.plus(Event.defaultDuration.toLong())
        val eventEnd = DateTimeUtils.getDateTimeString(endTime)

        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, event.startTime.millis)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, eventEnd)
            putExtra(CalendarContract.Events.TITLE, event.title)
            putExtra(CalendarContract.Events.DESCRIPTION, event.description)
            putExtra(CalendarContract.Events.EVENT_LOCATION, event.locality)
            // Indicates that this event is free time and will not conflict with other events
            putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_FREE)
        }

        startActivity(intent)
    }

    private fun openMaps(event: Event) {
        val url = "http://maps.google.co.in/maps?q=${event.locality}"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    private fun displayAddToCalendarDialog() {
        val context = requireContext()

        val calendars = arrayOf(
                getString(R.string.external_calendar),
                getString(R.string.tum_calendar)
        )

        val dialog = AlertDialog.Builder(context)
                .setTitle(R.string.add_to_calendar_info)
                .setSingleChoiceItems(calendars, 0, null)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.add) { _, which ->
                    handleCalendarExportSelection(which)
                }
                .setCancelable(true)
                .create()

        dialog.window?.setBackgroundDrawableResource(R.drawable.rounded_corners_background)
        dialog.show()
    }

    private fun handleCalendarExportSelection(which: Int) {
        when(which) {
            0 -> addToExternalCalendar()
            else -> addToTUMCalendar()
        }
    }

    companion object {

        @JvmStatic
        fun newInstance(event: Event): EventDetailsFragment {
            return EventDetailsFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(Const.KEY_EVENT, event)
                }
            }
        }

    }

}
