package de.tum.`in`.tumcampusapp.component.tumui.roomfinder

import android.os.Bundle
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.generic.activity.BaseActivity
import de.tum.`in`.tumcampusapp.component.tumui.roomfinder.NavigationDetailsFragment.Companion.NAVIGATION_ENTITY_ID

class NavigationDetailsActivity : BaseActivity(
    R.layout.activity_search
) {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var navigationEntityID = ""
        if (savedInstanceState == null) {
            val bundle = intent.extras
            if (bundle != null) {
                navigationEntityID = bundle.get(NAVIGATION_ENTITY_ID) as String
            }
        } else {
            navigationEntityID = savedInstanceState.getSerializable(NAVIGATION_ENTITY_ID) as String
        }

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.contentFrame, NavigationDetailsFragment.newInstance(navigationEntityID))
            .commit()
    }
}
