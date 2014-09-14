package de.tum.in.tumcampus.activities.generic;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.auxiliary.ImplicitCounter;
import de.tum.in.tumcampus.auxiliary.StickyListViewDelegate;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;
import uk.co.senab.actionbarpulltorefresh.extras.actionbarcompat.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

/**
 * Generic class which handles can handle a long running background task
 */
public abstract class ProgressActivity extends ActionBarActivity implements OnRefreshListener {

    /** Default layouts for user interaction */
    private int mLayoutId;
    private boolean mFirstFetch = true;
    protected RelativeLayout errorLayout;
    protected RelativeLayout progressLayout;
    protected PullToRefreshLayout refreshLayout;

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
        if(refreshLayout !=null) {
            ActionBarPullToRefresh.from(this).allChildrenArePullable()
                    .useViewDelegate(StickyListHeadersListView.class, new StickyListViewDelegate())
                    .listener(this).setup(refreshLayout);
        }

		if (progressLayout == null || errorLayout == null) {
			Log.e(getClass().getSimpleName(), "Cannot find layouts, did you forget to provide error and progress layouts?");
		}
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void showLoadingStart() {
        if(mFirstFetch || refreshLayout ==null) {
            progressLayout.setVisibility(View.VISIBLE);
            mFirstFetch = false;
        } else {
            refreshLayout.setRefreshing(true);
        }
    }

    public void showError(int errorReason) {
        showError(getString(errorReason));
    }

    public void showError(String errorReason) {
        showLoadingEnded();
        Toast.makeText(this, errorReason, Toast.LENGTH_SHORT).show();
        errorLayout.setVisibility(View.VISIBLE);
    }

    public void showErrorLayout() {
		errorLayout.setVisibility(View.VISIBLE);
	}

	public void showProgressLayout() {
		progressLayout.setVisibility(View.VISIBLE);
	}

    public void hideErrorLayout() {
        errorLayout.setVisibility(View.GONE);
    }

    public void hideProgressLayout() {
        progressLayout.setVisibility(View.GONE);
    }

    public void showLoadingEnded() {
        errorLayout.setVisibility(View.GONE);
        progressLayout.setVisibility(View.GONE);
        if(refreshLayout !=null) {
            refreshLayout.setRefreshComplete();
        }
    }

    protected void enableRefresh() {
        if(refreshLayout !=null)
            refreshLayout.setEnabled(true);
    }

    protected void disableRefresh() {
        if(refreshLayout !=null)
            refreshLayout.setEnabled(false);
    }

    @Override
    public void onRefreshStarted(View view) {}
}
