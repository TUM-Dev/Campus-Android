package de.tum.in.tumcampusapp.activities.generic;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.auxiliary.Utils;

public abstract class ActivityForSearching extends Activity implements OnEditorActionListener {

	protected RelativeLayout progressLayout;
	protected RelativeLayout errorLayout;
	protected EditText searchField;
	private int layoutId;

	// Abstract method for the search algorithm, has to be implemented by the
	// inheriting class
	public abstract boolean performSearchAlgorithm();

	public ActivityForSearching(int layoutIt) {
		this.layoutId = layoutIt;
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

	public void onClick(View view) {
		int viewId = view.getId();
		switch (viewId) {
		case R.id.dosearch:
			requestSearch();
			hideKeyboard();
			break;
		case R.id.clear:
			searchField.setText("");
			break;
		}
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		requestSearch();
		return false;
	}

	private void hideKeyboard() {
		((InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(searchField.getWindowToken(), 0);
	}

	private boolean requestSearch() {
		progressLayout.setVisibility(View.VISIBLE);
		if (searchField.getText().length() < 3) {
			Toast.makeText(this, R.string.please_insert_at_least_three_chars, Toast.LENGTH_SHORT).show();
			errorLayout.setVisibility(View.VISIBLE);
			progressLayout.setVisibility(View.GONE);
			return false;
		}

		if (!Utils.isConnected(this)) {
			Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
			errorLayout.setVisibility(View.VISIBLE);
			progressLayout.setVisibility(View.GONE);
			return false;
		}
		// set the query string as parameter for the TUMOnline request
		Utils.hideKeyboard(this, searchField);
		boolean searchSucceeded;
		searchSucceeded= performSearchAlgorithm();
		
		if (searchSucceeded) {
			errorLayout.setVisibility(View.GONE);
			progressLayout.setVisibility(View.GONE);
			return true;
		}
		return false;
	}
}
