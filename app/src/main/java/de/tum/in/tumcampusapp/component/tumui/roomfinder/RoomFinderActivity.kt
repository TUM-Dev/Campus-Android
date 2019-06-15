package de.tum.`in`.tumcampusapp.component.tumui.roomfinder

import android.os.Bundle
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.generic.activity.BaseActivity

class RoomFinderActivity : BaseActivity(R.layout.activity_roomfinder) {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.contentFrame, RoomFinderFragment.newInstance())
                .commit()
        }
    }

}
