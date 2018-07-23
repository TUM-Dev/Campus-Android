package de.tum.in.tumcampusapp.component.other.generic.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.AppCompatButton;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.other.settings.UserPreferencesActivity;
import de.tum.in.tumcampusapp.utils.NetUtils;
import de.tum.in.tumcampusapp.utils.Utils;

/**
 * Generic class which handles can handle a long running background task
 */
public abstract class ProgressActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {

    /**
     * Default layouts for user interaction
     */
    private FrameLayout allErrorsLayout;
    protected LinearLayout errorLayout;
    private FrameLayout progressLayout;
    private LinearLayout noTokenLayout;
    protected LinearLayout noInternetLayout;
    protected LinearLayout failedTokenLayout;
    protected SwipeRefreshLayout swipeRefreshLayout;
    private boolean registered;

    /**
     * Standard constructor for ProgressActivity.
     * The given layout must include a errors_layout.
     * If the Activity should support Pull-To-Refresh it can also contain a
     * {@link SwipeRefreshLayout} named ptr_layout
     *
     * @param layoutId Resource id of the xml layout that should be used to inflate the activity
     */
    public ProgressActivity(int layoutId) {
        super(layoutId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get handles to all error layouts
        allErrorsLayout = findViewById(R.id.errors_layout);
        progressLayout = findViewById(R.id.progress_layout);
        errorLayout = findViewById(R.id.error_layout);
        swipeRefreshLayout = findViewById(R.id.ptr_layout);
        noInternetLayout = findViewById(R.id.no_internet_layout);
        failedTokenLayout = findViewById(R.id.failed_layout);
        noTokenLayout = findViewById(R.id.no_token_layout);

        AppCompatButton retryButton = findViewById(R.id.retry_button);
        if (retryButton != null) {
            retryButton.setOnClickListener(v -> {
                showLoadingStart();
                onRefresh();
            });
        }

        // If content is refreshable setup the SwipeRefreshLayout
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(this);
            swipeRefreshLayout.setColorSchemeResources(
                    R.color.color_primary,
                    R.color.tum_A100,
                    R.color.tum_A200);
        }

        if (progressLayout == null) {
            Utils.log("Cannot find layouts, did you forget to provide all_error_layout?");
        }
    }

    /**
     * Shows error layout and toasts the given message.
     * Hides any progress indicator.
     *
     * @param errorReason Resource id of the error text
     */
    protected void showError(int errorReason) {
        showError(getString(errorReason));
    }

    /**
     * Shows error layout and toasts the given message.
     * Hides any progress indicator.
     *
     * @param errorReason Error text
     */
    protected void showError(String errorReason) {
        TextView error = errorLayout.findViewById(R.id.error_text);
        error.setText(errorReason);
        showErrorLayout();
    }

    /**
     * Shows error layout.
     * Hides any progress indicator.
     */
    protected void showErrorLayout() {
        showLoadingEnded();
        errorLayout.setVisibility(View.VISIBLE);
        allErrorsLayout.setVisibility(View.VISIBLE);
    }

    protected void showFailedTokenLayout(int resId) {
        showFailedTokenLayout(getString(resId));
    }

    /**
     * Shows failed layout
     *
     * @param error Error Text to be toasted
     */
    protected void showFailedTokenLayout(String error) {
        showLoadingEnded();
        failedTokenLayout.setVisibility(View.VISIBLE);
        allErrorsLayout.setVisibility(View.VISIBLE);
    }

    /**
     * Shows failed layout
     */
    protected void showNoTokenLayout() {
        showLoadingEnded();

        AppCompatButton settingsButton = findViewById(R.id.open_settings_button);
        settingsButton.setOnClickListener(v -> startActivity(
                new Intent(this, UserPreferencesActivity.class)));

        noTokenLayout.setVisibility(View.VISIBLE);
        allErrorsLayout.setVisibility(View.VISIBLE);
        Utils.log("No token was set");
    }

    /**
     * Shows failed layout
     */
    protected void showNoInternetLayout() {
        showLoadingEnded();
        noInternetLayout.setVisibility(View.VISIBLE);
        allErrorsLayout.setVisibility(View.VISIBLE);
        noInternetLayout.findViewById(R.id.progressWifi)
                        .setVisibility(View.INVISIBLE);

        WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifi == null) {
            return;
        }

        AppCompatButton wifiButton = findViewById(R.id.button_enable_wifi);
        wifiButton.setVisibility(wifi.isWifiEnabled() ? View.GONE : View.VISIBLE);

        registerReceiver(connectivityChangeReceiver,
                         new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        registered = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (registered) {
            unregisterReceiver(connectivityChangeReceiver);
            registered = false;
        }
    }

    /**
     * Shows progress layout or sets {@link SwipeRefreshLayout}'s state to refreshing
     * if present in the xml layout
     */
    protected void showLoadingStart() {
        if (registered) {
            unregisterReceiver(connectivityChangeReceiver);
            registered = false;
        }

        if (swipeRefreshLayout == null) {
            noInternetLayout.setVisibility(View.GONE);
            noTokenLayout.setVisibility(View.GONE);
            errorLayout.setVisibility(View.GONE);
            progressLayout.setVisibility(View.VISIBLE);
            allErrorsLayout.setVisibility(View.VISIBLE);
        } else {
            swipeRefreshLayout.setRefreshing(true);
        }
    }

    /**
     * Indicates that the background progress ended by hiding error and progress layout
     * and setting {@link SwipeRefreshLayout}'s state to completed
     */
    protected void showLoadingEnded() {
        failedTokenLayout.setVisibility(View.GONE);
        noInternetLayout.setVisibility(View.GONE);
        noTokenLayout.setVisibility(View.GONE);
        errorLayout.setVisibility(View.GONE);
        progressLayout.setVisibility(View.GONE);
        allErrorsLayout.setVisibility(View.GONE);
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    /**
     * Enables {@link SwipeRefreshLayout}
     */
    protected void enableRefresh() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setEnabled(true);
        }
    }

    /**
     * Disables {@link SwipeRefreshLayout}
     */
    protected void disableRefresh() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setEnabled(false);
        }
    }

    /**
     * Gets called when Pull-To-Refresh layout was used to refresh content.
     * Should start the background refresh task.
     * Override this if you use a {@link SwipeRefreshLayout}
     */
    @Override
    public void onRefresh() {
        // Subclasses can override this
    }

    /**
     * Show wifi settings
     */
    public void onEnableWifi(View view) {
        WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifi != null) {
            wifi.setWifiEnabled(true);
        }

        noInternetLayout.findViewById(R.id.progressWifi)
                        .setVisibility(View.VISIBLE);
    }

    final BroadcastReceiver connectivityChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (NetUtils.isConnected(context)) {
                onRefresh();
            }
        }
    };
}
