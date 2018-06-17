package de.tum.`in`.tumcampusapp.component.tumui.calendar

import android.app.SearchManager
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.tumui.calendar.model.CalendarItem
import de.tum.`in`.tumcampusapp.component.tumui.roomfinder.RoomFinderActivity
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.utils.Const.CALENDAR_ID_PARAM
import de.tum.`in`.tumcampusapp.utils.Utils
import kotlinx.android.synthetic.main.fragment_calendar_details.*

class CalendarDetailsFragment : BottomSheetDialogFragment() {

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
            deleteButton.setOnClickListener {
                (activity as CalendarActivity).deleteEvent(calendarItem.nr)
            }
            editButton.setOnClickListener { (activity as CalendarActivity).editEvent(calendarItem)}
        } else {
            buttonsContainer.visibility = View.GONE
        }
    }

    private fun onLocationClicked(location: String) {
        val findStudyRoomIntent = Intent()
        findStudyRoomIntent.putExtra(SearchManager.QUERY, Utils.extractRoomNumberFromLocation(location))
        findStudyRoomIntent.setClass(context, RoomFinderActivity::class.java)
        startActivity(findStudyRoomIntent)
    }

}