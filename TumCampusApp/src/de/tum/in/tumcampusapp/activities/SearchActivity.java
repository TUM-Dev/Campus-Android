package de.tum.in.tumcampusapp.activities;

import java.util.List;

import android.app.SearchManager;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.actionbarsherlock.widget.SearchView.OnQueryTextListener;
import com.actionbarsherlock.widget.SearchView.OnSuggestionListener;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.data.SearchAction;
import de.tum.in.tumcampusapp.data.SearchLecture;
import de.tum.in.tumcampusapp.data.SearchPerson;
import de.tum.in.tumcampusapp.tumonline.TUMOnlineRequest;
import de.tum.in.tumcampusapp.tumonline.TUMOnlineRequestFetchListener;

/**
 * 
 * 
 */
public class SearchActivity extends SherlockFragmentActivity implements OnQueryTextListener, OnSuggestionListener, TUMOnlineRequestFetchListener {

	private TUMOnlineRequest requestHandler;

	/** UI Elements */
	private ListView lvFound;
	private ListView lvCategories;

	/** Default layouts for user interaction */
	private RelativeLayout noTokenLayout;
	private RelativeLayout progressLayout;
	private RelativeLayout emptyLayout;
	private RelativeLayout errorLayout;
	private RelativeLayout failedTokenLayout;

	/** Our specifc settings for the indiviudal types */
	private List<SearchAction> listHandles;

	public static final String searchProp = "searchThis";

	private class SuggestionsAdapter extends CursorAdapter {

		public SuggestionsAdapter(Context context, Cursor c) {
			super(context, c, 0);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			TextView tv = (TextView) view;
			final int textIndex = cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1);
			tv.setText(cursor.getString(textIndex));
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			LayoutInflater inflater = LayoutInflater.from(context);
			View v = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
			return v;
		}
	}

	private static final String[] COLUMNS = { BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1, };

	private SuggestionsAdapter mSuggestionsAdapter;

	private SearchView searchView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set our layout
		this.setContentView(R.layout.activity_search);

		// If valid inflate the resulting layout with the results
		this.progressLayout = (RelativeLayout) this.findViewById(R.id.progress_layout);
		this.failedTokenLayout = (RelativeLayout) this.findViewById(R.id.failed_layout);
		this.noTokenLayout = (RelativeLayout) this.findViewById(R.id.no_token_layout);
		this.errorLayout = (RelativeLayout) this.findViewById(R.id.error_layout);
		this.emptyLayout = (RelativeLayout) this.findViewById(R.id.empty_layout);

		// bind GUI elements
		this.lvFound = (ListView) this.findViewById(R.id.lvFound);

		// Create our settings for the different search actions
		this.listHandles.add(new SearchPerson(this));
		this.listHandles.add(new SearchLecture(this));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Create the search view
		this.searchView = new SearchView(this.getSupportActionBar().getThemedContext());
		this.searchView.setQueryHint("Search...");
		this.searchView.setOnQueryTextListener(this);
		this.searchView.setOnSuggestionListener(this);

		if (this.mSuggestionsAdapter == null) {
			MatrixCursor cursor = new MatrixCursor(COLUMNS);
			cursor.addRow(new String[] { "1", "'Murica" });
			cursor.addRow(new String[] { "2", "Canada" });
			cursor.addRow(new String[] { "3", "Denmark" });
			this.mSuggestionsAdapter = new SuggestionsAdapter(this.getSupportActionBar().getThemedContext(), cursor);
		}

		this.searchView.setSuggestionsAdapter(this.mSuggestionsAdapter);
		this.searchView.setIconifiedByDefault(false);
		this.searchView.setFocusable(true);
		this.searchView.setIconified(false);
		this.searchView.requestFocusFromTouch();

		// Add to bar
		MenuItem m = menu.add("Search");

		m.setIcon(R.drawable.abs__ic_search).setActionView(this.searchView).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		m.expandActionView();

		// Get search value from intent
		Bundle extras = this.getIntent().getExtras();
		if (extras != null) {
			String value = extras.getString(SearchActivity.searchProp);
			// this.searchView.setQuery(value, true);
		}

		return true;
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		return false;
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		Toast.makeText(this, "You searched for: " + query, Toast.LENGTH_LONG).show();
		// Hide the keyboard
		// Utils.hideKeyboard(this, this.rootView);

		// .doSearch(query);

		return true;
	}

	@Override
	public boolean onSuggestionClick(int position) {
		Cursor c = (Cursor) this.mSuggestionsAdapter.getItem(position);
		String query = c.getString(c.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1));
		Toast.makeText(this, "Suggestion clicked: " + query, Toast.LENGTH_LONG).show();
		return true;
	}

	@Override
	public boolean onSuggestionSelect(int position) {
		return false;
	}

	private void showError(boolean show) {
		if (show) {
			this.hideAll();
			this.errorLayout.setVisibility(View.VISIBLE);
		} else {
			this.errorLayout.setVisibility(View.GONE);
		}

	}

	private void showProgress(boolean show) {
		if (show) {
			this.hideAll();
			this.progressLayout.setVisibility(View.VISIBLE);
		} else {
			this.progressLayout.setVisibility(View.GONE);
		}
	}

	private void showNoTok(boolean show) {
		if (show) {
			this.hideAll();
			this.noTokenLayout.setVisibility(View.VISIBLE);
		} else {
			this.noTokenLayout.setVisibility(View.GONE);
		}
	}

	private void showTokErr(boolean show) {
		if (show) {
			this.hideAll();
			this.failedTokenLayout.setVisibility(View.VISIBLE);
		} else {
			this.failedTokenLayout.setVisibility(View.GONE);
		}
	}

	private void showEmpty(boolean show) {
		if (show) {
			this.hideAll();
			this.emptyLayout.setVisibility(View.VISIBLE);
		} else {
			this.emptyLayout.setVisibility(View.GONE);
		}
	}

	private void hideAll() {
		this.showError(false);
		this.showNoTok(false);
		this.showProgress(false);
		this.showTokErr(false);
		this.showEmpty(false);
	}

	public void doSearch(SearchAction action, String query) {
		// If we don't have our handle do nothing
		if (action == null) {
			Log.d(this.getClass().getSimpleName(), "No action supplied ");
			return;
		}

		// Create our new handle
		this.requestHandler = new TUMOnlineRequest(action.getTumAction(), this);

		// set the query string as parameter for the TUMOnline request
		this.requestHandler.setParameter("pSuche", query);

		// Check if we have a Token
		String accessToken = PreferenceManager.getDefaultSharedPreferences(this).getString(Const.ACCESS_TOKEN, null);
		if (accessToken == null) {
			Log.i(this.getClass().getSimpleName(), "No token was set");
			this.showNoTok(true);
			return;
		}

		// Do the pull
		Log.i(this.getClass().getSimpleName(), "TUMOnline token is <" + accessToken + ">");
		this.showProgress(true);
		this.requestHandler.fetchInteractive(this, this);
	}

	@Override
	// SearchAction action,
	public void onFetch(String rawResponse) {
		BaseAdapter adapter = null;
		try {
			adapter = this.action.handleResponse(rawResponse);
		} catch (Exception e) {
			Log.d(this.getClass().getSimpleName(), "wont work: " + e.getMessage());
			this.showError(true);
			Toast.makeText(this, R.string.no_search_result, Toast.LENGTH_SHORT).show();
		}

		if (adapter == null) {
			this.lvFound.setAdapter(null);
			this.showEmpty(true);
			Log.d(this.getClass().getSimpleName(), "Returned Adapter empty");
			return;
		}
		Log.d(this.getClass().getSimpleName(), "Returned Adapter has " + adapter.getCount());

		// Setup the adapter
		this.lvFound.setAdapter(adapter);

		// deal with clicks on items in the ListView
		this.lvFound.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> a, View v, int position, long id) {
				// each item represents the current FindLecturesRow
				// item
				/*
				 * Object o = SearchFragment.this.lvFound.getItemAtPosition(position);
				 * 
				 * Intent i = new Intent(SearchFragment.this.activity, SearchFragment.this.action.getDetailsActivity());
				 * i.putExtras(SearchFragment.this.action.getBundle(o)); // load LectureDetails SearchFragment.this.startActivity(i);
				 */
			}
		});

		// Hide all messages
		this.hideAll();
	}

	@Override
	public void onFetchCancelled() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCommonError(String errorReason) {
		Toast.makeText(this, errorReason, Toast.LENGTH_SHORT).show();
		this.showError(true);
	}

	@Override
	public void onFetchError(String errorReason) {
		Toast.makeText(this, errorReason, Toast.LENGTH_SHORT).show();
		this.showTokErr(true);
	}

}
