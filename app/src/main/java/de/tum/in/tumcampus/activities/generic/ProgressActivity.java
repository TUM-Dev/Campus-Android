package de.tum.in.tumcampus.activities.generic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.UserPreferencesActivity;
import de.tum.in.tumcampus.auxiliary.NetUtils;
import de.tum.in.tumcampus.auxiliary.Utils;

/**
 * Generic class which handles can handle a long running background task
 */
public abstract class ProgressActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {

    /**
     * Default layouts for user interaction
     */
    private LinearLayout allErrorsLayout;
    private RelativeLayout errorLayout;
    private RelativeLayout progressLayout;
    private RelativeLayout noTokenLayout;
    private RelativeLayout noInternetLayout;
    private RelativeLayout failedTokenLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private boolean registered = false;
    private Handler mLoadingHandler = new Handler();

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
        allErrorsLayout = (LinearLayout) findViewById(R.id.errors_layout);
        progressLayout = (RelativeLayout) findViewById(R.id.progress_layout);
        errorLayout = (RelativeLayout) findViewById(R.id.error_layout);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.ptr_layout);
        noInternetLayout = (RelativeLayout) findViewById(R.id.no_internet_layout);
        failedTokenLayout = (RelativeLayout) findViewById(R.id.failed_layout);
        noTokenLayout = (RelativeLayout) findViewById(R.id.no_token_layout);

        // If content is refreshable setup the SwipeRefreshLayout
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(this);
            //TODO refresh layout and end refreshing
            //ActionBarPullToRefresh.from(this).allChildrenArePullable()
            //.useViewDelegate(StickyListHeadersListView.class, new StickyListViewDelegate())
            //        .listener(this).setup(refreshLayout);
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
        TextView error = (TextView) errorLayout.findViewById(R.id.error_text);
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

    /**
     * Shows custom error layout.
     * Hides any progress indicator.
     *
     * @param customErrorLayout Error layout
     */
    protected void showCustomErrorLayout(RelativeLayout customErrorLayout) {
        showLoadingEnded();
        customErrorLayout.setVisibility(View.VISIBLE);
        allErrorsLayout.setVisibility(View.VISIBLE);
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
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
    }

    /**
     * Shows failed layout
     */
    protected void showNoTokenLayout() {
        showLoadingEnded();
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
        noInternetLayout.findViewById(R.id.progressWifi).setVisibility(View.INVISIBLE);
        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        Button but = (Button) findViewById(R.id.button_enable_wifi);
        but.setVisibility(wifi.isWifiEnabled() ? View.GONE : View.VISIBLE);
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
            mLoadingHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(true);
                }
            }, 1000);
        }
    }

    /**
     * Indicates that the background progress ended by hiding error and progress layout
     * and setting {@link SwipeRefreshLayout}'s state to completed
     */
    protected void showLoadingEnded() {
        mLoadingHandler.removeCallbacksAndMessages(null);
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
        if (swipeRefreshLayout != null)
            swipeRefreshLayout.setEnabled(true);
    }

    /**
     * Disables {@link SwipeRefreshLayout}
     */
    protected void disableRefresh() {
        if (swipeRefreshLayout != null)
            swipeRefreshLayout.setEnabled(false);
    }

    /**
     * Gets called when Pull-To-Refresh layout was used to refresh content.
     * Should start the background refresh task.
     * Override this if you use a {@link SwipeRefreshLayout}
     */
    @Override
    public abstract void onRefresh();

    /**
     * Handle click on error_layout, failed_layout and no_token_layout
     *
     * @param view Handle of layout view
     */
    public void onClick(View view) {
        int viewId = view.getId();
        switch (viewId) {
            case R.id.failed_layout:
            case R.id.error_layout:
                onRefresh();
                break;
            case R.id.no_token_layout:
                startActivity(new Intent(this, UserPreferencesActivity.class));
                break;
        }
    }

    /**
     * Show wifi settings
     */
    public void onEnableWifi(View view) {
        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifi.setWifiEnabled(true);
        noInternetLayout.findViewById(R.id.progressWifi).setVisibility(View.VISIBLE);
    }

    BroadcastReceiver connectivityChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (NetUtils.isConnected(context)) {
                onRefresh();
            }
        }
    };
}
