package de.tum.`in`.tumcampusapp.component.ui.onboarding

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.core.content.edit
import com.google.gson.Gson
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.api.app.AuthenticationManager
import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.api.app.model.TUMCabeVerification
import de.tum.`in`.tumcampusapp.api.app.model.UploadStatus
import de.tum.`in`.tumcampusapp.api.tumonline.AccessTokenManager
import de.tum.`in`.tumcampusapp.component.other.generic.fragment.FragmentForLoadingInBackground
import de.tum.`in`.tumcampusapp.component.ui.onboarding.di.OnboardingComponent
import de.tum.`in`.tumcampusapp.component.ui.onboarding.di.OnboardingComponentProvider
import de.tum.`in`.tumcampusapp.databinding.FragmentOnboardingExtrasBinding
import de.tum.`in`.tumcampusapp.service.SilenceService
import de.tum.`in`.tumcampusapp.utils.CacheManager
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.NetUtils
import de.tum.`in`.tumcampusapp.utils.Utils
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.support.v4.browse
import java.io.IOException
import javax.inject.Inject

class OnboardingExtrasFragment : FragmentForLoadingInBackground<Boolean>(
    R.layout.fragment_onboarding_extras,
    R.string.connect_to_tum_online
) {

    private val onboardingComponent: OnboardingComponent by lazy {
        (requireActivity() as OnboardingComponentProvider).onboardingComponent()
    }

    @Inject
    lateinit var cacheManager: CacheManager

    @Inject
    lateinit var tumCabeClient: TUMCabeClient

    @Inject
    lateinit var authManager: AuthenticationManager

    @Inject
    lateinit var navigator: OnboardingNavigator

    private val binding: FragmentOnboardingExtrasBinding by viewBinding(FragmentOnboardingExtrasBinding::bind)

    override val swipeRefreshLayout get() = binding.swipeRefreshLayout
    override val layoutAllErrorsBinding get() = binding.layoutAllErrors

    override fun onAttach(context: Context) {
        super.onAttach(context)
        onboardingComponent.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            bugReportsCheckBox.isChecked = Utils.getSettingBool(requireContext(), Const.BUG_REPORTS, true)

            if (AccessTokenManager.hasValidAccessToken(requireContext())) {
                silentModeCheckBox.isChecked =
                    Utils.getSettingBool(requireContext(), Const.SILENCE_SERVICE, false)
                silentModeCheckBox.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked && !SilenceService.hasPermissions(requireContext())) {
                        SilenceService.requestPermissions(requireContext())
                        silentModeCheckBox.isChecked = false
                    }
                }
            } else {
                silentModeCheckBox.isChecked = false
                silentModeCheckBox.isEnabled = false
            }

            if (AccessTokenManager.hasValidAccessToken(requireContext())) {
                cacheManager.fillCache()
            }

            privacyPolicyButton.setOnClickListener { browse(getString(R.string.url_privacy_policy)) }
            finishButton.setOnClickListener { startLoading() }
        }
    }

    override fun onLoadInBackground(): Boolean {
        if (!NetUtils.isConnected(requireContext())) {
            showNoInternetLayout()
            return false
        }

        // By now, we should have generated the RSA key and uploaded it to our server and TUMonline
        val status = tumCabeClient.verifyKey()
        if (status?.status != UploadStatus.VERIFIED) {
            Utils.showToastOnUIThread(requireActivity(), getString(R.string.error_pk_verification))
            return false
        }

        // Try to restore already joined chat rooms from server
        try {
            TUMCabeVerification.create(requireContext(), null) // TODO is this needed?

            val lrzId = Utils.getSetting(requireContext(), Const.LRZ_ID, "") // TODO is this needed?
            // upload obfuscated ids now that we have a member
            val uploadStatus = tumCabeClient.getUploadStatus(lrzId)
            if (uploadStatus != null) {
                authManager.uploadObfuscatedIds(uploadStatus)
            }
            return true
        } catch (e: IOException) {
            Utils.log(e)
            return false
        }
    }

    override fun onLoadFinished(result: Boolean) {
        if (!result) {
            showLoadingEnded()
            return
        }

        // Gets the editor for editing preferences and updates the preference values with the
        // chosen state
        requireContext()
            .defaultSharedPreferences
            .edit {
                    putBoolean(Const.SILENCE_SERVICE, binding.silentModeCheckBox.isChecked)
                    putBoolean(Const.BACKGROUND_MODE, true) // Enable by default
                    putBoolean(Const.BUG_REPORTS, binding.bugReportsCheckBox.isChecked)
            }

        navigator.finish()
    }

    private fun SharedPreferences.Editor.put(key: String, value: Any) {
        putString(key, Gson().toJson(value))
    }

    companion object {
        fun newInstance() = OnboardingExtrasFragment()
    }
}
