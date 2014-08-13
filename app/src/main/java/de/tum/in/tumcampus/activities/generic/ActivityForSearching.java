package de.tum.in.tumcampus.activities.generic;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.auxiliary.Utils;

/**
 * Generic class for searching. Provides basic functions for a text search field
 * and typical processes related to search.
 * 
 * @author Sascha Moecker
 * 
 */
public abstract class ActivityForSearching extends ActionBarActivity implements
		OnEditorActionListener {

	protected RelativeLayout errorLayout;
	private int layoutId;
	protected RelativeLayout progressLayout;
	protected EditText searchField;

	public ActivityForSearching(int layoutIt) {
		this.layoutId = layoutIt;
	}

	public void onClick(View view) {
		int viewId = view.getId();
		switch (viewId) {
		case R.id.dosearch:
			requestSearch();
			Utils.hideKeyboard(this, searchField);
			break;
		case R.id.clear:
			searchField.setText("");
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
		setContentView(layoutId);

		progressLayout = (RelativeLayout) findViewById(R.id.progress_layout);
		errorLayout = (RelativeLayout) findViewById(R.id.error_layout);
		searchField = (EditText) findViewById(R.id.search_field);

		searchField.setOnEditorActionListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_activity_for_searching, menu);
		return true;
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		requestSearch();
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
		progressLayout.setVisibility(View.VISIBLE);
		if (searchField.getText().length() < 3) {
			Toast.makeText(this, R.string.please_insert_at_least_three_chars,
					Toast.LENGTH_SHORT).show();
			errorLayout.setVisibility(View.VISIBLE);
			progressLayout.setVisibility(View.GONE);
			return false;
		}

		if (!Utils.isConnected(this)) {
			Toast.makeText(this, R.string.no_internet_connection,
					Toast.LENGTH_SHORT).show();
			errorLayout.setVisibility(View.VISIBLE);
			progressLayout.setVisibility(View.GONE);
			return false;
		}
		// set the query string as parameter for the TUMOnline request
		Utils.hideKeyboard(this, searchField);
		performSearchAlgorithm();
		return true;
	}
}
