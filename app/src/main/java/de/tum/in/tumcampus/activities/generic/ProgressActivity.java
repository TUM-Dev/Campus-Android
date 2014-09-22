package de.tum.in.tumcampus.activities.generic;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import de.tum.in.tumcampus.R;
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
    private RelativeLayout errorLayout;
    private RelativeLayout progressLayout;
    private PullToRefreshLayout refreshLayout;

    /**
     * Saves if this is the first time data is fetched
     * If a {@link PullToRefreshLayout} is given it doesn't
     * show the progress as fullscreen progressbar but as refresh indicator
     */
    private boolean mFirstFetch = true;

    /**
     * Standard constructor for ProgressActivity.
     * The given layout must include a progress_layout and an error_layout.
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

        progressLayout = (RelativeLayout) findViewById(R.id.progress_layout);
        errorLayout = (RelativeLayout) findViewById(R.id.error_layout);
        refreshLayout = (PullToRefreshLayout) findViewById(R.id.ptr_layout);

        // If content is refreshable setup the PullToRefreshLayout
        if (refreshLayout != null) {
            ActionBarPullToRefresh.from(this).allChildrenArePullable()
                    .useViewDelegate(StickyListHeadersListView.class, new StickyListViewDelegate())
                    .listener(this).setup(refreshLayout);
        }

        if (progressLayout == null || errorLayout == null) {
            Utils.log("Cannot find layouts, did you forget to provide error and progress layouts?");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
    }

    /**
     * Shows progress layout
     */
    protected void showProgressLayout() {
        progressLayout.setVisibility(View.VISIBLE);
    }

    /**
     * Hides error layout
     */
    public void hideErrorLayout() {
        errorLayout.setVisibility(View.GONE);
    }

    /**
     * Hides progress layout
     */
    protected void hideProgressLayout() {
        progressLayout.setVisibility(View.GONE);
    }

    /**
     * Shows progress layout or sets {@link PullToRefreshLayout}'s state to refreshing
     * if present in the xml layout
     */
    protected void showLoadingStart() {
        if (mFirstFetch || refreshLayout == null) {
            errorLayout.setVisibility(View.GONE);
            progressLayout.setVisibility(View.VISIBLE);
            mFirstFetch = false;
        } else {
            refreshLayout.setRefreshing(true);
        }
    }

    /**
     * Indicates that the background progress ended by hiding error and progress layout
     * and setting {@link PullToRefreshLayout}'s state to completed
     */
    protected void showLoadingEnded() {
        errorLayout.setVisibility(View.GONE);
        progressLayout.setVisibility(View.GONE);
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
    public void onRefreshStarted(View view) {
    }

    /**
     * Handle click on error_layout
     * @param view Handle of error_layout
     */
    public abstract void onClick(View view);
}
