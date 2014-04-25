package de.tum.in.tumcampusapp.activities.generic;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.auxiliary.Utils;

/**
 * Generic class for searching. Provides basic functions for a text search field and typical processes related to search.
 * 
 * @author Sascha Moecker
 * 
 */
public abstract class ActivityForSearching extends SherlockFragmentActivity implements OnEditorActionListener {

	protected RelativeLayout errorLayout;
	private int layoutId;
	protected RelativeLayout progressLayout;
	protected EditText searchField;

	/**
	 * 
	 * @param layoutId
	 *            default layout of this activity
	 */
	public ActivityForSearching(int layoutId) {
		this.layoutId = layoutId;
	}

	public void onClick(View view) {
		int viewId = view.getId();
		switch (viewId) {
		case R.id.dosearch:
			this.requestSearch();
			Utils.hideKeyboard(this, this.searchField);
			break;
		case R.id.clear:
			this.searchField.setText("");
			break;
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(this.layoutId);

		this.progressLayout = (RelativeLayout) this.findViewById(R.id.progress_layout);
		this.errorLayout = (RelativeLayout) this.findViewById(R.id.error_layout);
		this.searchField = (EditText) this.findViewById(R.id.search_field);

		this.searchField.setOnEditorActionListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		// Inflate the menu; this adds items to the action bar if it is present.
		this.getSupportMenuInflater().inflate(R.menu.menu_activity_for_searching, menu);
		return true;
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		this.requestSearch();
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	// Abstract method for the search algorithm, has to be implemented by the
	// inheriting class
	public abstract boolean performSearchAlgorithm();

	private boolean requestSearch() {
		this.progressLayout.setVisibility(View.VISIBLE);
		if (this.searchField.getText().length() < 3) {
			Toast.makeText(this, R.string.please_insert_at_least_three_chars, Toast.LENGTH_SHORT).show();
			this.errorLayout.setVisibility(View.VISIBLE);
			this.progressLayout.setVisibility(View.GONE);
			return false;
		}

		if (!Utils.isConnected(this)) {
			Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
			this.errorLayout.setVisibility(View.VISIBLE);
			this.progressLayout.setVisibility(View.GONE);
			return false;
		}
		// set the query string as parameter for the TUMOnline request
		Utils.hideKeyboard(this, this.searchField);
		this.performSearchAlgorithm();
		return true;
	}
}
