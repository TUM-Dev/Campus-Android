package de.tum.`in`.tumcampusapp.component.tumui.calendar

import android.app.SearchManager
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.support.design.widget.FloatingActionButton
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.tumui.calendar.model.CalendarItem
import de.tum.`in`.tumcampusapp.component.tumui.roomfinder.RoomFinderActivity
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.utils.Const.CALENDAR_ID_PARAM
import de.tum.`in`.tumcampusapp.utils.Utils

class CalendarDetailsFragment : BottomSheetDialogFragment() {

    private lateinit var dao: CalendarDao

    private lateinit var calendarId: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_calendar_details, container, false)
        calendarId = arguments?.getString(CALENDAR_ID_PARAM)!!
        dao = TcaDb.getInstance(context).calendarDao()
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val calendarItem = dao.getCalendarItemById(calendarId)
        updateView(calendarItem, view)
    }

    private fun updateView(calendarItem: CalendarItem, view: View) {
        val titleTextView = view.findViewById<TextView>(R.id.bottomSheetHeading)
        val dateTextView = view.findViewById<TextView>(R.id.bottomSheetDateText)
        val locationTextView = view.findViewById<TextView>(R.id.bottomSheetLocationText)
        val descriptionTextView = view.findViewById<TextView>(R.id.bottomSheetDescriptionText)
        val deleteButton = view.findViewById<Button>(R.id.bottomSheetDeleteEvent)
        val editButton = view.findViewById<Button>(R.id.bottomSheetEditEvent)
        val buttons = view.findViewById<View>(R.id.bottomSheetEditButtons)
        val floatingButton = view.findViewById<FloatingActionButton>(R.id.bottomSheetFloatingButton)
        val floatingButtonCanceled = view.findViewById<View>(R.id.bottomSheetFloatingButtonCanceled)

        if(calendarItem.status.equals("CANCEL")) {
            floatingButton.visibility = View.GONE
            floatingButtonCanceled.visibility = View.VISIBLE
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

        if (calendarItem.description.isEmpty()){
            descriptionTextView.visibility = View.GONE
        } else {
            descriptionTextView.text = calendarItem.description
        }

        locationTextView.setOnClickListener { onLocationClicked(calendarItem.location) }

        if (calendarItem.url.isEmpty()) {
            buttons.visibility = View.VISIBLE
            deleteButton.setOnClickListener {
                (activity as CalendarActivity).deleteEvent(calendarItem.nr)
            }
            editButton.setOnClickListener { (activity as CalendarActivity).editEvent(calendarItem)}
        } else {
            buttons.visibility = View.GONE
        }
    }

    private fun onLocationClicked(location: String) {
        val findStudyRoomIntent = Intent()
        findStudyRoomIntent.putExtra(SearchManager.QUERY, Utils.extractRoomNumberFromLocation(location))
        findStudyRoomIntent.setClass(context, RoomFinderActivity::class.java)
        startActivity(findStudyRoomIntent)
    }
}