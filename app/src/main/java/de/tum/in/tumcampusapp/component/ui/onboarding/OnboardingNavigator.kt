package de.tum.`in`.tumcampusapp.component.ui.onboarding

import android.content.Intent
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.transaction
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.ui.onboarding.di.OnboardingScope
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Utils
import javax.inject.Inject

@OnboardingScope
class OnboardingNavigator @Inject constructor(
    private val activity: OnboardingActivity
) {

    private var didFinishFlow = false

    private val fragmentManager: FragmentManager
        get() = activity.supportFragmentManager

    fun openNext() {
        val destination = when (val current = fragmentManager.findFragmentById(R.id.contentFrame)) {
            is OnboardingStartFragment -> CheckTokenFragment.newInstance()
            is CheckTokenFragment -> OnboardingExtrasFragment.newInstance()
            else -> throw IllegalStateException("Invalid fragment ${current?.javaClass?.simpleName}")
        }

        fragmentManager.transaction {
            replace(R.id.contentFrame, destination)
            addToBackStack(null)
        }
    }

    fun finish() {
        didFinishFlow = true
        val intent = Intent(activity, StartupActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        activity.startActivity(intent)
        activity.finishAndRemoveTask()
    }

    fun onClose() {
        if (!didFinishFlow) {
            // The user opened the onboarding screen and maybe filled out some information, but did
            // not finish it completely.
            Utils.setSetting(activity, Const.LRZ_ID, "")
            Utils.setSetting(activity, Const.ACCESS_TOKEN, "")
        }
    }

}
