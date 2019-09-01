package de.tum.`in`.tumcampusapp.component.other.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.transaction
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceFragmentCompat.OnPreferenceStartScreenCallback
import androidx.preference.PreferenceScreen
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.generic.activity.BaseActivity
import de.tum.`in`.tumcampusapp.utils.Const

class SettingsActivity : BaseActivity(R.layout.activity_user_preferences),
    OnPreferenceStartScreenCallback {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            val rootKey = intent.getStringExtra(Const.PREFERENCE_SCREEN)
            supportFragmentManager.transaction {
                replace(R.id.settings_frame, SettingsFragment.newInstance(rootKey))
            }
        }
    }

    override fun onPreferenceStartScreen(
        preferenceFragment: PreferenceFragmentCompat,
        preferenceScreen: PreferenceScreen
    ): Boolean {
        supportFragmentManager.transaction {
            replace(R.id.settings_frame, SettingsFragment.newInstance(preferenceScreen.key))
            addToBackStack(null)
        }

        return true
    }

    companion object {

        fun newIntent(
            context: Context,
            rootKey: String
        ) = Intent(context, SettingsActivity::class.java).apply {
            putExtra(Const.PREFERENCE_SCREEN, rootKey)
        }
    }
}