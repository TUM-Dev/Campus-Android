package de.tum.`in`.tumcampusapp.component.tumui.calendar

import android.os.Bundle

import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.generic.activity.ActivityForAccessingTumOnline
import de.tum.`in`.tumcampusapp.component.tumui.calendar.model.EventsResponse
import de.tum.`in`.tumcampusapp.utils.Const

/**
 * Activity showing the user's calendar. Calendar items (events) are fetched from TUMOnline and displayed as blocks on a timeline.
 */
class CalendarActivity : ActivityForAccessingTumOnline<EventsResponse>(R.layout.activity_calendar) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            val showDate = intent.getLongExtra(Const.EVENT_TIME, -1)
            val eventId = intent.getStringExtra(Const.KEY_EVENT_ID)
            val fragment = CalendarFragment.newInstance(showDate, eventId)

            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.contentFrame, fragment)
                    .commit()
        }
    }
}
