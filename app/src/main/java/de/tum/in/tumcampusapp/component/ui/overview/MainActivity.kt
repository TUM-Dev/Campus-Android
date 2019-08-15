package de.tum.`in`.tumcampusapp.component.ui.overview

import android.os.Bundle
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.generic.activity.BaseActivity
import de.tum.`in`.tumcampusapp.utils.Utils

/**
 * Main activity displaying the cards and providing navigation with navigation drawer
 */
class MainActivity : BaseActivity(R.layout.activity_main) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.log("MainActivity created")

        if (savedInstanceState == null) {
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.contentFrame, MainFragment.newInstance())
                    .commit()
        }
    }

}
