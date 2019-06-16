package de.tum.`in`.tumcampusapp.component.ui.onboarding

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.transaction
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.generic.activity.BaseActivity
import de.tum.`in`.tumcampusapp.utils.Backpressable

class OnboardingActivity : BaseActivity(R.layout.activity_onboarding) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            supportFragmentManager.transaction {
                replace(R.id.contentFrame, OnboardingStartFragment.newInstance())
            }
        }
    }

    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentById(R.id.contentFrame) ?: return
        if (fragment is Backpressable && fragment.onBackPressed()) {
            return
        }

        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
            return
        }

        super.onBackPressed()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        fun newIntent(context: Context) = Intent(context, OnboardingActivity::class.java)
    }

}
