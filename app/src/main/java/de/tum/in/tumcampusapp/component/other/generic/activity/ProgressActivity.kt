package de.tum.`in`.tumcampusapp.component.other.generic.activity

import android.graphics.Color
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkRequest
import android.os.Bundle
import android.support.design.button.MaterialButton
import android.support.design.widget.Snackbar
import android.support.v4.widget.SwipeRefreshLayout
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.api.tumonline.exception.*
import de.tum.`in`.tumcampusapp.component.other.generic.viewstates.*
import de.tum.`in`.tumcampusapp.utils.NetUtils.internetCapability
import de.tum.`in`.tumcampusapp.utils.setImageResourceOrHide
import de.tum.`in`.tumcampusapp.utils.setTextOrHide
import org.jetbrains.anko.connectivityManager
import java.net.UnknownHostException

/**
 * Generic class which handles can handle a long running background task
 */
abstract class ProgressActivity(
        layoutId: Int
) : BaseActivity(layoutId), SwipeRefreshLayout.OnRefreshListener {

    private val contentView: ViewGroup by lazy {
        findViewById<ViewGroup>(android.R.id.content).getChildAt(0) as ViewGroup
    }

    protected val swipeRefreshLayout: SwipeRefreshLayout? by lazy {
        findViewById<SwipeRefreshLayout>(R.id.ptr_layout)
    }

    private val errorLayoutsContainer: FrameLayout by lazy {
        findViewById<FrameLayout>(R.id.errors_layout)
    }

    private val errorLayout: LinearLayout by lazy {
        findViewById<LinearLayout>(R.id.error_layout)
    }

    private val errorIconImageView: ImageView by lazy {
        findViewById<ImageView>(R.id.iconImageView)
    }

    private val errorHeaderTextView: TextView by lazy {
        errorLayout.findViewById<TextView>(R.id.headerTextView)
    }

    private val errorMessageTextView: TextView by lazy {
        errorLayout.findViewById<TextView>(R.id.messageTextView)
    }

    private val errorButton: MaterialButton by lazy {
        errorLayout.findViewById<MaterialButton>(R.id.button)
    }

    private val progressLayout: FrameLayout by lazy {
        findViewById<FrameLayout>(R.id.progress_layout)
    }

    private var registered: Boolean = false

    private val networkCallback: NetworkCallback = object : NetworkCallback() {
        override fun onAvailable(network: Network?) {
            runOnUiThread(this@ProgressActivity::onRefresh)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // If content is refreshable setup the SwipeRefreshLayout
        swipeRefreshLayout?.apply {
            setOnRefreshListener(this@ProgressActivity)
            setColorSchemeResources(
                    R.color.color_primary,
                    R.color.tum_A100,
                    R.color.tum_A200
            )
        }
    }

    /**
     * Shows error layout and toasts the given message.
     * Hides any progress indicator.
     *
     * @param messageResId Resource id of the error text
     */
    protected fun showError(messageResId: Int) {
        runOnUiThread {
            showError(UnknownErrorViewState(messageResId))
        }
    }

    protected fun showErrorSnackbar(t: Throwable) {
        val messageResId = when (t) {
            is UnknownHostException -> R.string.no_internet_connection
            is InactiveTokenException -> R.string.error_access_token_inactive
            is InvalidTokenException -> R.string.error_invalid_access_token
            is MissingPermissionException -> R.string.error_no_rights_to_access_function
            is TokenLimitReachedException -> R.string.error_access_token_limit_reached
            is RequestLimitReachedException -> R.string.error_request_limit_reached
            else -> R.string.error_unknown
        }

        showErrorSnackbar(messageResId)
    }

    protected fun showErrorSnackbar(messageResId: Int) {
        runOnUiThread {
            Snackbar.make(contentView, messageResId, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry) { retryRequest() }
                    .setActionTextColor(Color.WHITE)
                    .show()
        }
    }

    protected fun showErrorLayout(throwable: Throwable) {
        when (throwable) {
            is UnknownHostException -> showNoInternetLayout()
            is InactiveTokenException -> showFailedTokenLayout(R.string.error_access_token_inactive)
            is InvalidTokenException -> showFailedTokenLayout(R.string.error_invalid_access_token)
            is MissingPermissionException -> showFailedTokenLayout(R.string.error_no_rights_to_access_function)
            is TokenLimitReachedException -> showFailedTokenLayout(R.string.error_access_token_limit_reached)
            is RequestLimitReachedException -> showError(R.string.error_request_limit_reached)
            else -> showError(R.string.error_unknown)
        }
    }

    protected fun showFailedTokenLayout(messageResId: Int = R.string.error_accessing_tumonline_body) {
        runOnUiThread {
            showError(FailedTokenViewState(messageResId))
        }
    }

    protected fun showNoInternetLayout() {
        runOnUiThread {
            showError(NoInternetViewState())
        }

        val request = NetworkRequest.Builder()
                .addCapability(internetCapability)
                .build()
        connectivityManager.registerNetworkCallback(request, networkCallback)
        registered = true
    }

    protected fun showEmptyResponseLayout(messageResId: Int, iconResId: Int? = null) {
        runOnUiThread {
            showError(EmptyViewState(iconResId, messageResId))
        }
    }

    protected fun showErrorLayout() {
        runOnUiThread {
            errorLayout.visibility = View.VISIBLE
        }
    }

    private fun showError(viewState: ErrorViewState) {
        showLoadingEnded()

        errorIconImageView.setImageResourceOrHide(viewState.iconResId)
        errorHeaderTextView.setTextOrHide(viewState.headerResId)
        errorMessageTextView.setTextOrHide(viewState.messageResId)

        errorButton.setTextOrHide(viewState.buttonTextResId)
        errorButton.setOnClickListener { retryRequest() }

        errorLayoutsContainer.visibility = View.VISIBLE
        errorLayout.visibility = View.VISIBLE
    }

    /**
     * Shows progress layout or sets [SwipeRefreshLayout]'s state to refreshing
     * if present in the xml layout
     */
    protected fun showLoadingStart() {
        if (registered) {
            connectivityManager.unregisterNetworkCallback(networkCallback)
            registered = false
        }

        swipeRefreshLayout?.let {
            it.isRefreshing = true
            return
        }

        errorLayoutsContainer.visibility = View.VISIBLE
        errorLayout.visibility = View.GONE
        progressLayout.visibility = View.VISIBLE
    }

    /**
     * Indicates that the background progress ended by hiding error and progress layout
     * and setting [SwipeRefreshLayout]'s state to completed
     */
    protected fun showLoadingEnded() {
        errorLayoutsContainer.visibility = View.GONE
        progressLayout.visibility = View.GONE
        errorLayout.visibility = View.GONE
        swipeRefreshLayout?.isRefreshing = false
    }

    /**
     * Enables [SwipeRefreshLayout]
     */
    protected fun enableRefresh() {
        swipeRefreshLayout?.isEnabled = true
    }

    /**
     * Disables [SwipeRefreshLayout]
     */
    protected fun disableRefresh() {
        swipeRefreshLayout?.isEnabled = false
    }

    /**
     * Gets called when Pull-To-Refresh layout was used to refresh content.
     * Should start the background refresh task.
     * Override this if you use a [SwipeRefreshLayout]
     */
    override fun onRefresh() {
        // Free ad space
    }

    private fun retryRequest() {
        showLoadingStart()
        onRefresh()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (registered) {
            connectivityManager.unregisterNetworkCallback(networkCallback)
            registered = false
        }
    }

}
