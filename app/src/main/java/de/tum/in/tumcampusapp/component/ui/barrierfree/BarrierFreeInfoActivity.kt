package de.tum.`in`.tumcampusapp.component.ui.barrierfree

import android.os.Bundle

import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.generic.activity.BaseActivity

class BarrierFreeInfoActivity : BaseActivity(R.layout.activity_barrier_free_info) {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.contentFrame, BarrierFreeInfoFragment.newInstance())
                    .commit()
        }
    }

}
