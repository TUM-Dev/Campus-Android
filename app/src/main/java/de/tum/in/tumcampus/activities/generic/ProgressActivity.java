package de.tum.in.tumcampus.activities.generic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.UserPreferencesActivity;
import de.tum.in.tumcampus.auxiliary.ImplicitCounter;
import de.tum.in.tumcampus.auxiliary.StickyListViewDelegate;
import de.tum.in.tumcampus.auxiliary.Utils;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;
import uk.co.senab.actionbarpulltorefresh.extras.actionbarcompat.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

/**
 * Generic class which handles can handle a long running background task
 */
public abstract class ProgressActivity extends ActionBarActivity implements OnRefreshListener {

    /**
     * Default layouts for user interaction
     */
    private final int mLayoutId;
    private LinearLayout allErrorsLayout;
    private RelativeLayout errorLayout;
    private RelativeLayout progressLayout;
    private RelativeLayout noTokenLayout;
    private RelativeLayout noInternetLayout;
    private RelativeLayout failedTokenLayout;
    private PullToRefreshLayout refreshLayout;
    private boolean registered = false;

    /**
     * Standard constructor for ProgressActivity.
     * The given layout must include a errors_layout.
     * If the Activity should support Pull-To-Refresh it can also contain a
     * {@link PullToRefreshLayout} named ptr_layout
     *
     * @param layoutId Resource id of the xml layout that should be used to inflate the activity
     */
    public ProgressActivity(int layoutId) {
        mLayoutId = layoutId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImplicitCounter.Counter(this);
        setContentView(mLayoutId);

        allErrorsLayout = (LinearLayout) findViewById(R.id.errors_layout);
        progressLayout = (RelativeLayout) findViewById(R.id.progress_layout);
        errorLayout = (RelativeLayout) findViewById(R.id.error_layout);
        refreshLayout = (PullToRefreshLayout) findViewById(R.id.ptr_layout);
        noInternetLayout = (RelativeLayout) findViewById(R.id.no_internet_layout);
        failedTokenLayout = (RelativeLayout) findViewById(R.id.failed_layout);
        noTokenLayout = (RelativeLayout) findViewById(R.id.no_token_layout);

        // If content is refreshable setup the PullToRefreshLayout
        if (refreshLayout != null) {
            ActionBarPullToRefresh.from(this).allChildrenArePullable()
                    .useViewDelegate(StickyListHeadersListView.class, new StickyListViewDelegate())
                    .listener(this).setup(refreshLayout);
        }

        if (progressLayout == null) {
            Utils.log("Cannot find layouts, did you forget to provide all_error_layout?");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return false;
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
        Utils.showToast(this, errorReason);
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
        if(registered) {
            unregisterReceiver(connectivityChangeReceiver);
            registered = false;
        }
    }

    /**
     * Shows progress layout or sets {@link PullToRefreshLayout}'s state to refreshing
     * if present in the xml layout
     */
    protected void showLoadingStart() {
        if(registered) {
            unregisterReceiver(connectivityChangeReceiver);
            registered = false;
        }
        if (refreshLayout == null) {
            noInternetLayout.setVisibility(View.GONE);
            noTokenLayout.setVisibility(View.GONE);
            errorLayout.setVisibility(View.GONE);
            progressLayout.setVisibility(View.VISIBLE);
            allErrorsLayout.setVisibility(View.VISIBLE);
        } else {
            refreshLayout.setRefreshing(true);
        }
    }

    /**
     * Indicates that the background progress ended by hiding error and progress layout
     * and setting {@link PullToRefreshLayout}'s state to completed
     */
    protected void showLoadingEnded() {
        failedTokenLayout.setVisibility(View.GONE);
        noInternetLayout.setVisibility(View.GONE);
        noTokenLayout.setVisibility(View.GONE);
        errorLayout.setVisibility(View.GONE);
        progressLayout.setVisibility(View.GONE);
        allErrorsLayout.setVisibility(View.GONE);
        if (refreshLayout != null) {
            refreshLayout.setRefreshComplete();
        }
    }

    /**
     * Enables {@link PullToRefreshLayout}
     */
    protected void enableRefresh() {
        if (refreshLayout != null)
            refreshLayout.setEnabled(true);
    }

    /**
     * Disables {@link PullToRefreshLayout}
     */
    protected void disableRefresh() {
        if (refreshLayout != null)
            refreshLayout.setEnabled(false);
    }

    /**
     * Gets called when Pull-To-Refresh layout was used to refresh content.
     * Should start the background refresh task.
     * Override this if you use a {@link PullToRefreshLayout}
     *
     * @param view View that should be refreshed
     */
    @Override
    public abstract void onRefreshStarted(View view);

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
                onRefreshStarted(view);
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
    }

    BroadcastReceiver connectivityChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Utils.isConnected(context)) {
                onRefreshStarted(null);
            }
        }
    };
}
