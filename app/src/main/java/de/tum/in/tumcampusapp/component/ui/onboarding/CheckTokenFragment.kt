package de.tum.`in`.tumcampusapp.component.ui.onboarding

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.api.tumonline.TUMOnlineClient
import de.tum.`in`.tumcampusapp.api.tumonline.exception.InactiveTokenException
import de.tum.`in`.tumcampusapp.component.other.generic.fragment.BaseFragment
import de.tum.`in`.tumcampusapp.component.tumui.person.model.IdentitySet
import de.tum.`in`.tumcampusapp.component.ui.onboarding.di.OnboardingComponent
import de.tum.`in`.tumcampusapp.component.ui.onboarding.di.OnboardingComponentProvider
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Utils
import de.tum.`in`.tumcampusapp.utils.plusAssign
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_check_token.nextButton
import kotlinx.android.synthetic.main.fragment_check_token.openTumOnlineButton
import org.jetbrains.anko.support.v4.browse
import java.net.UnknownHostException
import javax.inject.Inject

sealed class IdentityResponse {
    data class Success(val identity: IdentitySet) : IdentityResponse()
    data class Failure(val throwable: Throwable) : IdentityResponse()
}

class CheckTokenFragment : BaseFragment<Unit>(
    R.layout.fragment_check_token,
    R.string.connect_to_tum_online
) {

    private val compositeDisposable = CompositeDisposable()

    private val onboardingComponent: OnboardingComponent by lazy {
        (requireActivity() as OnboardingComponentProvider).onboardingComponent()
    }

    @Inject
    lateinit var tumOnlineClient: TUMOnlineClient

    @Inject
    lateinit var navigator: OnboardingNavigator

    override fun onAttach(context: Context) {
        super.onAttach(context)
        onboardingComponent.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        disableRefresh()
        openTumOnlineButton.setOnClickListener { browse(Const.TUM_CAMPUS_URL) }
        nextButton.setOnClickListener { loadIdentitySet() }
    }

    private fun loadIdentitySet() {
        val toast = Toast.makeText(requireContext(), R.string.checking_if_token_enabled, Toast.LENGTH_LONG)
        toast.show()

        compositeDisposable += tumOnlineClient.getIdentity()
            .map { IdentityResponse.Success(it) as IdentityResponse }
            .doOnError(Utils::log)
            .onErrorReturn { IdentityResponse.Failure(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { response ->
                toast.cancel()
                when (response) {
                    is IdentityResponse.Success -> handleDownloadSuccess(response.identity)
                    is IdentityResponse.Failure -> handleDownloadFailure(response.throwable)
                }
            }
    }

    private fun handleDownloadSuccess(identitySet: IdentitySet) {
        val identity = identitySet.ids.first()
        Utils.setSetting(requireContext(), Const.CHAT_ROOM_DISPLAY_NAME, identity.toString())

        val ids = identity.obfuscated_ids

        // Save the TUMonline ID to preferences
        // Switch to identity.getObfuscated_id() in the future
        Utils.setSetting(requireContext(), Const.TUMO_PIDENT_NR, ids.studierende)
        Utils.setSetting(requireContext(), Const.TUMO_STUDENT_ID, ids.studierende)
        Utils.setSetting(requireContext(), Const.TUMO_EMPLOYEE_ID, ids.bedienstete)
        Utils.setSetting(requireContext(), Const.TUMO_EXTERNAL_ID, ids.extern) // usually alumni

        if (ids.bedienstete.isNotEmpty() && ids.studierende.isEmpty() && ids.extern.isEmpty()) {
            Utils.setSetting(requireContext(), Const.EMPLOYEE_MODE, true)
            // only preset cafeteria prices if the user is only an employee
            // since we can't determine which id is active (given once and never removed)
            Utils.setSetting(requireContext(), Const.ROLE, "1")
        }

        // Note: we can't upload the obfuscated ids here since we might not have a (chat) member yet

        navigator.openNext()
    }

    private fun handleDownloadFailure(t: Throwable) {
        val messageResId = when (t) {
            is UnknownHostException -> R.string.no_internet_connection
            is InactiveTokenException -> R.string.error_access_token_inactive
            else -> R.string.error_unknown
        }

        Utils.showToast(requireContext(), messageResId)
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }

    companion object {
        fun newInstance() = CheckTokenFragment()
    }
}
