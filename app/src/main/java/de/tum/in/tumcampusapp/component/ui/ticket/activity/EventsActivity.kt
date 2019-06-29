package de.tum.`in`.tumcampusapp.component.ui.ticket.activity

import android.os.Bundle

import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.generic.activity.BaseActivity

class EventsActivity : BaseActivity(R.layout.activity_events) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.contentFrame, EventsFragment.newInstance())
                    .commit()
        }
    }

}


