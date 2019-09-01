package de.tum.`in`.tumcampusapp.component.ui.studyroom

import android.os.Bundle
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.generic.activity.BaseActivity

/**
 * Shows information about reservable study rooms.
 */
class StudyRoomsActivity : BaseActivity(
        R.layout.activity_study_rooms
) {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.contentFrame, StudyRoomsFragment.newInstance())
                .commit()
        }
    }
}
