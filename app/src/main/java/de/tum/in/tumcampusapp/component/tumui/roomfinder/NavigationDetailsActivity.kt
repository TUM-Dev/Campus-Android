package de.tum.`in`.tumcampusapp.component.tumui.roomfinder

import android.os.Bundle
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.api.navigatum.domain.NavigationEntity
import de.tum.`in`.tumcampusapp.component.other.generic.activity.BaseActivity
import de.tum.`in`.tumcampusapp.component.tumui.roomfinder.NavigationDetailsFragment.Companion.NAVIGATION_ENTITY

class NavigationDetailsActivity : BaseActivity(
    R.layout.activity_search
) {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var navigationEntity = NavigationEntity()
        if (savedInstanceState == null) {
            val bundle = intent.extras
            if (bundle != null) {
                navigationEntity = bundle.get(NAVIGATION_ENTITY) as NavigationEntity
            }
        } else {
            navigationEntity = savedInstanceState.getSerializable(NAVIGATION_ENTITY) as NavigationEntity
        }

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.contentFrame, NavigationDetailsFragment.newInstance(navigationEntity))
            .commit()
    }
}
