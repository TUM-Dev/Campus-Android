package de.tum.`in`.tumcampusapp.component.ui.onboarding

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.jakewharton.rxbinding3.widget.textChanges
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.api.app.AuthenticationManager
import de.tum.`in`.tumcampusapp.api.app.exception.NoPublicKey
import de.tum.`in`.tumcampusapp.api.tumonline.AccessTokenManager
import de.tum.`in`.tumcampusapp.api.tumonline.TUMOnlineClient
import de.tum.`in`.tumcampusapp.api.tumonline.exception.InactiveTokenException
import de.tum.`in`.tumcampusapp.api.tumonline.exception.InvalidTokenException
import de.tum.`in`.tumcampusapp.api.tumonline.exception.RequestLimitReachedException
import de.tum.`in`.tumcampusapp.api.tumonline.exception.TokenLimitReachedException
import de.tum.`in`.tumcampusapp.api.tumonline.exception.UnknownErrorException
import de.tum.`in`.tumcampusapp.api.tumonline.model.AccessToken
import de.tum.`in`.tumcampusapp.component.other.generic.fragment.BaseFragment
import de.tum.`in`.tumcampusapp.component.ui.onboarding.di.OnboardingComponent
import de.tum.`in`.tumcampusapp.component.ui.onboarding.di.OnboardingComponentProvider
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Utils
import de.tum.`in`.tumcampusapp.utils.plusAssign
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_onboarding_start.lrzIdTextView
import kotlinx.android.synthetic.main.fragment_onboarding_start.nextButton
import kotlinx.android.synthetic.main.toolbar.toolbar
import org.jetbrains.anko.inputMethodManager
import java.util.Locale
import javax.inject.Inject

sealed class TokenResponse {
    data class Success(val token: AccessToken) : TokenResponse()
    data class Failure(val t: Throwable) : TokenResponse()
}

class OnboardingStartFragment : BaseFragment<Unit>(
    R.layout.fragment_onboarding_start,
    R.string.connect_to_tum_online
) {

    private val compositeDisposable = CompositeDisposable()

    private val onboardingComponent: OnboardingComponent by lazy {
        (requireActivity() as OnboardingComponentProvider).onboardingComponent()
    }

    @Inject
    lateinit var authManager: AuthenticationManager

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
        setCustomCloseIcon()

        val lrzId = Utils.getSetting(requireContext(), Const.LRZ_ID, "")
        lrzIdTextView.setText(lrzId)

        compositeDisposable += lrzIdTextView.textChanges()
            .map { it.toString() }
            .subscribe {
                val isEmpty = it.isBlank()
                val alpha = if (isEmpty) 0.5f else 1.0f
                nextButton.isClickable = !isEmpty
                nextButton.alpha = alpha
            }

        nextButton.setOnClickListener { onNextPressed() }
    }

    private fun setCustomCloseIcon() {
        val closeIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_clear)
        if (closeIcon != null) {
            val color = ContextCompat.getColor(requireContext(), R.color.color_primary)
            closeIcon.setTint(color)
        }
        toolbar.navigationIcon = closeIcon
        toolbar.setNavigationOnClickListener { requireActivity().finish() }
    }

    private fun onNextPressed() {
        val enteredId = lrzIdTextView.text.toString().toLowerCase(Locale.GERMANY)

        if (!enteredId.matches(Const.TUM_ID_PATTERN.toRegex())) {
            Utils.showToast(requireContext(), R.string.error_invalid_tum_id)
            return
        }

        Utils.setSetting(requireContext(), Const.LRZ_ID, enteredId)
        hideKeyboard()

        if (AccessTokenManager.hasValidAccessToken(requireContext())) {
            AlertDialog.Builder(requireContext())
                .setMessage(getString(R.string.error_access_token_already_set_generate_new))
                .setPositiveButton(getString(R.string.generate_new_token)) { _, _ ->
                    generateNewToken(enteredId)
                }
                .setNegativeButton(getString(R.string.use_existing)) { _, _ ->
                    openNextOnboardingStep()
                }
                .create()
                .apply {
                    window?.setBackgroundDrawableResource(R.drawable.rounded_corners_background)
                }
                .show()
        } else {
            requestNewToken(enteredId)
        }
    }

    /**
     * Requests a new [AccessToken] with the provided public key.
     * @param publicKey The public key with which to request the [AccessToken]
     */
    private fun requestNewToken(publicKey: String) {
        showLoadingStart()
        val tokenName = "TUMCampusApp-" + Build.PRODUCT

        compositeDisposable += tumOnlineClient
            .requestToken(publicKey, tokenName)
            .map { TokenResponse.Success(it) as TokenResponse }
            .doOnError(Utils::log)
            .onErrorReturn { TokenResponse.Failure(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { response ->
                showLoadingEnded()
                when (response) {
                    is TokenResponse.Success -> handleTokenDownloadSuccess(response.token)
                    is TokenResponse.Failure -> handleTokenDownloadFailure(response.t)
                }
            }
    }

    /**
     * Called when the access token was downloaded successfully. This method stores the access
     * token, uploads the public key to TUMonline and opens the next step in the setup wizard.
     *
     * @param accessToken The downloaded [AccessToken]
     */
    private fun handleTokenDownloadSuccess(accessToken: AccessToken) {
        Utils.log("AcquiredAccessToken = " + accessToken.token)

        // Save access token to preferences
        Utils.setSetting(requireContext(), Const.ACCESS_TOKEN, accessToken.token)

        // Upload the secret to this new generated token
        try {
            authManager.uploadPublicKey()
        } catch (noPublicKey: NoPublicKey) {
            Utils.log(noPublicKey)
        }

        openNextOnboardingStep()
    }

    private fun handleTokenDownloadFailure(t: Throwable) {
        resetAccessToken()
        displayErrorDialog(t)
    }

    /**
     * Display an obtrusive error dialog because on the provided [Throwable].
     * @param throwable The [Throwable] that occurred
     */
    private fun displayErrorDialog(throwable: Throwable) {
        val messageResId = when (throwable) {
            is InactiveTokenException -> R.string.error_access_token_inactive
            is InvalidTokenException -> R.string.error_invalid_access_token
            is UnknownErrorException -> R.string.error_unknown
            is TokenLimitReachedException -> R.string.error_access_token_limit_reached
            is RequestLimitReachedException -> R.string.error_request_limit_reached
            else -> R.string.error_access_token_could_not_be_generated
        }

        AlertDialog.Builder(requireContext())
            .setMessage(messageResId)
            .setPositiveButton(R.string.ok, null)
            .setCancelable(true)
            .create()
            .apply {
                window?.setBackgroundDrawableResource(R.drawable.rounded_corners_background)
            }
            .show()
    }

    private fun generateNewToken(enteredId: String) {
        authManager.clearKeys()
        authManager.generatePrivateKey(null)
        requestNewToken(enteredId)
    }

    private fun resetAccessToken() {
        Utils.setSetting(requireContext(), Const.ACCESS_TOKEN, "")
    }

    private fun hideKeyboard() {
        requireContext().inputMethodManager.hideSoftInputFromWindow(lrzIdTextView.windowToken, 0)
    }

    private fun openNextOnboardingStep() {
        navigator.openNext()
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }

    companion object {
        fun newInstance() = OnboardingStartFragment()
    }
}
