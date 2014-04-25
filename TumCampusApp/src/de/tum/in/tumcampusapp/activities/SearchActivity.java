package de.tum.in.tumcampusapp.activities;

import android.app.SearchManager;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.actionbarsherlock.widget.SearchView.OnQueryTextListener;
import com.actionbarsherlock.widget.SearchView.OnSuggestionListener;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.adapters.SearchPagerAdapter;
import de.tum.in.tumcampusapp.fragments.SearchFragment;

/**
 * 
 * 
 */
public class SearchActivity extends SherlockFragmentActivity implements OnQueryTextListener, OnSuggestionListener, TabListener {

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

	private SearchPagerAdapter pagerAdapter;
	private ViewPager pager;
	private SearchView searchView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set our layout
		this.setContentView(R.layout.activity_search);

		ActionBar bar = this.getSupportActionBar();
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		bar.addTab(bar.newTab().setText("Rooms").setTabListener(this));
		bar.addTab(bar.newTab().setText("Persons").setTabListener(this));
		bar.addTab(bar.newTab().setText("LVs").setTabListener(this));

		// Get the pager
		this.pager = (ViewPager) this.findViewById(R.id.pager);
		this.pagerAdapter = new SearchPagerAdapter(this.getSupportFragmentManager());
		this.pager.setAdapter(this.pagerAdapter);
		this.pager.setCurrentItem(0);
		this.pager.setOnPageChangeListener(new SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				super.onPageSelected(position); //
				SearchActivity.this.getSupportActionBar().setSelectedNavigationItem(position);
			}
		});

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
			this.searchView.setQuery(value, true);
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
		for (SearchFragment x : this.pagerAdapter.pages.values()) {
			x.doSearch(query);
		}
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

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction transaction) {
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction transaction) {
		if (this.pager != null) {
			this.pager.setCurrentItem(tab.getPosition());
		}
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction transaction) {
	}

}
