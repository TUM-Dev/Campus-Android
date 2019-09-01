package de.tum.`in`.tumcampusapp.component.ui.onboarding

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.transaction
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.generic.activity.BaseActivity
import de.tum.`in`.tumcampusapp.component.ui.onboarding.di.OnboardingComponent
import de.tum.`in`.tumcampusapp.component.ui.onboarding.di.OnboardingComponentProvider
import javax.inject.Inject

class OnboardingActivity : BaseActivity(R.layout.activity_onboarding), OnboardingComponentProvider {

    private val onboardingComponent: OnboardingComponent by lazy {
        injector.onboardingComponent().create(this)
    }

    @Inject
    lateinit var navigator: OnboardingNavigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onboardingComponent.inject(this)

        if (savedInstanceState == null) {
            supportFragmentManager.transaction {
                replace(R.id.contentFrame, OnboardingStartFragment.newInstance())
            }
        }
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

    override fun onDestroy() {
        navigator.onClose()
        super.onDestroy()
    }

    override fun onboardingComponent() = onboardingComponent

    companion object {
        fun newIntent(context: Context) = Intent(context, OnboardingActivity::class.java)
    }
}
