package de.tum.`in`.tumcampusapp.component.tumui.calendar

import android.app.SearchManager
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.api.app.exception.NoNetworkConnectionException
import de.tum.`in`.tumcampusapp.api.tumonline.TUMOnlineClient
import de.tum.`in`.tumcampusapp.api.tumonline.exception.RequestLimitReachedException
import de.tum.`in`.tumcampusapp.component.tumui.calendar.model.CalendarItem
import de.tum.`in`.tumcampusapp.component.tumui.calendar.model.DeleteEventResponse
import de.tum.`in`.tumcampusapp.component.tumui.roomfinder.RoomFinderActivity
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Const.CALENDAR_ID_PARAM
import de.tum.`in`.tumcampusapp.utils.Utils
import de.tum.`in`.tumcampusapp.utils.ui.RoundedBottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_calendar_details.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CalendarDetailsFragment : RoundedBottomSheetDialogFragment() {

    private lateinit var listener: OnEventInteractionListener
    private lateinit var dao: CalendarDao
    private lateinit var calendarId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dao = TcaDb.getInstance(context).calendarDao()

        arguments?.let { args ->
            calendarId = args.getString(CALENDAR_ID_PARAM)
        }
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_calendar_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val calendarItem = dao.getCalendarItemById(calendarId)
        updateView(calendarItem)
    }

    private fun updateView(calendarItem: CalendarItem) {
        if (calendarItem.status == "CANCEL") {
            cancelButtonsContainer.visibility = View.VISIBLE
            descriptionTextView.setTextColor(Color.RED)
        }

        titleTextView.text = calendarItem.title
        dateTextView.text = calendarItem.getEventDateString()
        locationTextView.text = calendarItem.location

        if (calendarItem.location.isEmpty()) {
            locationTextView.visibility = View.GONE
        } else {
            locationTextView.visibility = View.VISIBLE
        }

        if (calendarItem.description.isEmpty()) {
            descriptionTextView.visibility = View.GONE
        } else {
            descriptionTextView.text = calendarItem.description
        }

        locationTextView.setOnClickListener { onLocationClicked(calendarItem.location) }

        if (calendarItem.url.isEmpty()) {
            buttonsContainer.visibility = View.VISIBLE
            deleteButton.setOnClickListener { displayDeleteDialog(calendarItem.nr) }
            editButton.setOnClickListener { listener.onEditEvent(calendarItem) }
        } else {
            buttonsContainer.visibility = View.GONE
        }
    }

    private fun displayDeleteDialog(eventId: String) {
        AlertDialog.Builder(requireContext())
                .setTitle(R.string.event_delete_title)
                .setMessage(R.string.delete_event_info)
                .setPositiveButton(R.string.delete) { _, _ -> deleteEvent(eventId) }
                .setNegativeButton(R.string.cancel, null)
                .show()
    }

    private fun deleteEvent(eventId: String) {
        val c = requireContext()
        TUMOnlineClient
                .getInstance(c)
                .deleteEvent(eventId)
                .enqueue(object : Callback<DeleteEventResponse> {
                    override fun onResponse(call: Call<DeleteEventResponse>,
                                            response: Response<DeleteEventResponse>) {
                        dismiss()
                        listener.onEventDeleted(eventId)
                    }

                    override fun onFailure(call: Call<DeleteEventResponse>, t: Throwable) {
                        handleDeleteEventError(t)
                    }
                })
    }

    private fun handleDeleteEventError(t: Throwable) {
        val c = requireContext()
        val messageResId = when (t) {
            is NoNetworkConnectionException -> R.string.error_no_internet_connection
            is RequestLimitReachedException -> R.string.error_request_limit_reached
            else -> R.string.error_unknown
        }
        Utils.showToast(c, messageResId)
    }

    private fun onLocationClicked(location: String) {
        val findStudyRoomIntent = Intent()
        findStudyRoomIntent.putExtra(SearchManager.QUERY, Utils.extractRoomNumberFromLocation(location))
        findStudyRoomIntent.setClass(context, RoomFinderActivity::class.java)
        startActivity(findStudyRoomIntent)
    }

    companion object {

        @JvmStatic
        fun newInstance(calendarItem: CalendarItem,
                        listener: OnEventInteractionListener): CalendarDetailsFragment {
            return CalendarDetailsFragment().apply {
                this.arguments = Bundle().apply {
                    putString(Const.CALENDAR_ID_PARAM, calendarItem.nr)
                }
                this.listener = listener
            }
        }

    }

    interface OnEventInteractionListener {
        fun onEventDeleted(eventId: String)
        fun onEditEvent(calendarItem: CalendarItem)
    }

}