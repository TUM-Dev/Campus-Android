package de.tum.`in`.tumcampusapp.component.ui.cafeteria.activity

import android.os.Bundle

import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.generic.activity.BaseActivity
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.fragment.CafeteriaFragment

class CafeteriaActivity : BaseActivity(R.layout.activity_cafeteria) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.contentFrame, CafeteriaFragment.newInstance())
                    .commit()
        }
    }

}
