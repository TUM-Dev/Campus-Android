package de.tum.`in`.tumcampusapp.component.tumui.grades

import android.content.Context
import android.content.Intent
import android.os.Bundle
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.generic.activity.BaseActivity

class GradesActivity : BaseActivity(R.layout.activity_grades) {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.contentFrame, GradesFragment.newInstance())
                .commit()
        }
    }

    companion object {

        // TODO Eventually use Intent to BaseActivity with Intent extra
        fun newIntent(context: Context) = Intent(context, GradesActivity::class.java)
    }
}
