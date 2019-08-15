package de.tum.`in`.tumcampusapp.component.tumui.lectures.activity

import android.os.Bundle

import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.generic.activity.BaseActivity
import de.tum.`in`.tumcampusapp.component.tumui.lectures.fragment.LecturesFragment

/**
 * This activity presents the user's lectures. The results can be filtered by the semester.
 * This activity uses the same models as FindLectures.
 *
 * HINT: a TUMOnline access token is needed
 */
class LecturesPersonalActivity : BaseActivity(R.layout.activity_lectures) {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.contentFrame, LecturesFragment.newInstance())
                    .commit()
        }
    }

}
