package de.tum.`in`.tumcampusapp.component.ui.onboarding

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.core.content.edit
import com.google.gson.Gson
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.api.app.AuthenticationManager
import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.api.app.model.TUMCabeVerification
import de.tum.`in`.tumcampusapp.api.app.model.UploadStatus
import de.tum.`in`.tumcampusapp.api.tumonline.AccessTokenManager
import de.tum.`in`.tumcampusapp.component.other.generic.fragment.FragmentForLoadingInBackground
import de.tum.`in`.tumcampusapp.component.ui.chat.ChatRoomController
import de.tum.`in`.tumcampusapp.component.ui.chat.model.ChatMember
import de.tum.`in`.tumcampusapp.component.ui.onboarding.di.OnboardingComponent
import de.tum.`in`.tumcampusapp.component.ui.onboarding.di.OnboardingComponentProvider
import de.tum.`in`.tumcampusapp.service.SilenceService
import de.tum.`in`.tumcampusapp.utils.CacheManager
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.NetUtils
import de.tum.`in`.tumcampusapp.utils.Utils
import kotlinx.android.synthetic.main.fragment_onboarding_extras.bugReportsCheckBox
import kotlinx.android.synthetic.main.fragment_onboarding_extras.finishButton
import kotlinx.android.synthetic.main.fragment_onboarding_extras.groupChatCheckBox
import kotlinx.android.synthetic.main.fragment_onboarding_extras.privacyPolicyButton
import kotlinx.android.synthetic.main.fragment_onboarding_extras.silentModeCheckBox
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.support.v4.browse
import java.io.IOException
import javax.inject.Inject

class OnboardingExtrasFragment : FragmentForLoadingInBackground<ChatMember>(
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
    lateinit var chatRoomController: ChatRoomController

    @Inject
    lateinit var authManager: AuthenticationManager

    @Inject
    lateinit var navigator: OnboardingNavigator

    override fun onAttach(context: Context) {
        super.onAttach(context)
        onboardingComponent.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
            groupChatCheckBox.isChecked =
                Utils.getSettingBool(requireContext(), Const.GROUP_CHAT_ENABLED, true)
        } else {
            groupChatCheckBox.isChecked = false
            groupChatCheckBox.isEnabled = false
        }

        if (AccessTokenManager.hasValidAccessToken(requireContext())) {
            cacheManager.fillCache()
        }

        privacyPolicyButton.setOnClickListener { browse(getString(R.string.url_privacy_policy)) }
        finishButton.setOnClickListener { startLoading() }
    }

    override fun onLoadInBackground(): ChatMember? {
        if (!NetUtils.isConnected(requireContext())) {
            showNoInternetLayout()
            return null
        }

        // By now, we should have generated the RSA key and uploaded it to our server and TUMonline

        val lrzId = Utils.getSetting(requireContext(), Const.LRZ_ID, "")
        val name = Utils.getSetting(requireContext(),
            Const.CHAT_ROOM_DISPLAY_NAME, getString(R.string.not_connected_to_tumonline))

        val currentChatMember = ChatMember(lrzId)
        currentChatMember.displayName = name

        if (currentChatMember.lrzId.isNullOrEmpty()) {
            return currentChatMember
        }

        // Tell the server the new member
        val member: ChatMember?
        try {
            // After the user has entered their display name, create the new member on the server
            member = tumCabeClient.createMember(currentChatMember)
        } catch (e: IOException) {
            Utils.log(e)
            Utils.showToastOnUIThread(requireActivity(), R.string.error_setup_chat_member)
            return null
        }

        // Catch a possible error, when we didn't get something returned
        if (member?.lrzId == null) {
            Utils.showToastOnUIThread(requireActivity(), R.string.error_setup_chat_member)
            return null
        }

        val status = tumCabeClient.verifyKey()
        if (status?.status != UploadStatus.VERIFIED) {
            Utils.showToastOnUIThread(requireActivity(), getString(R.string.error_pk_verification))
            return null
        }

        // Try to restore already joined chat rooms from server
        return try {
            val verification = TUMCabeVerification.create(requireContext(), null)
            val rooms = tumCabeClient.getMemberRooms(member.id, verification)
            chatRoomController.replaceIntoRooms(rooms)

            // upload obfuscated ids now that we have a member
            val uploadStatus = tumCabeClient.getUploadStatus(lrzId)
            if (uploadStatus != null) {
                authManager.uploadObfuscatedIds(uploadStatus)
            }

            member
        } catch (e: IOException) {
            Utils.log(e)
            null
        }
    }

    override fun onLoadFinished(result: ChatMember?) {
        if (result == null) {
            showLoadingEnded()
            return
        }

        // Gets the editor for editing preferences and updates the preference values with the
        // chosen state
        requireContext()
            .defaultSharedPreferences
            .edit {
                putBoolean(Const.SILENCE_SERVICE, silentModeCheckBox.isChecked)
                putBoolean(Const.BACKGROUND_MODE, true) // Enable by default
                putBoolean(Const.BUG_REPORTS, bugReportsCheckBox.isChecked)

                if (!result.lrzId.isNullOrEmpty()) {
                    putBoolean(Const.GROUP_CHAT_ENABLED, groupChatCheckBox.isChecked)
                    putBoolean(Const.AUTO_JOIN_NEW_ROOMS, groupChatCheckBox.isChecked)
                    put(Const.CHAT_MEMBER, result)
                }
            }

        finishOnboarding()
    }

    private fun finishOnboarding() {
        navigator.finish()
    }

    private fun SharedPreferences.Editor.put(key: String, value: Any) {
        putString(key, Gson().toJson(value))
    }

    companion object {
        fun newInstance() = OnboardingExtrasFragment()
    }

}
