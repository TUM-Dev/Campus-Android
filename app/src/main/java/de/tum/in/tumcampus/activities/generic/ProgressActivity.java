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

/**
 * Generic class which handles can handle a long running background task
 */
public abstract class ProgressActivity extends ActionBarActivity {

    /** Default layouts for user interaction */
    private int mLayoutId;
    protected RelativeLayout errorLayout;
    protected RelativeLayout progressLayout;

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

    public void showError(int errorReason) {
        showError(getString(errorReason));
    }

    public void showError(String errorReason) {
        Toast.makeText(this, errorReason, Toast.LENGTH_SHORT).show();
        progressLayout.setVisibility(View.GONE);
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
}
