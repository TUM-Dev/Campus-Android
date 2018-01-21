package de.tum.`in`.tumcampusapp.fragments

import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.activities.RoomFinderActivity
import de.tum.`in`.tumcampusapp.auxiliary.Const.CALENDAR_ID_PARAM
import de.tum.`in`.tumcampusapp.auxiliary.Utils
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.database.dao.CalendarDao
import de.tum.`in`.tumcampusapp.models.tumo.CalendarItem

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

        titleTextView.text = calendarItem.title
        dateTextView.text = calendarItem.getEventDateString()
        locationTextView.text = calendarItem.location
        descriptionTextView.text = calendarItem.description
        locationTextView.setOnClickListener { onLocationClicked(calendarItem.location) }
    }

    private fun onLocationClicked(location: String) {
        val findStudyRoomIntent = Intent()
        findStudyRoomIntent.putExtra(SearchManager.QUERY, Utils.extractRoomNumberFromLocation(location))
        findStudyRoomIntent.setClass(context, RoomFinderActivity::class.java)
        startActivity(findStudyRoomIntent)
    }
}