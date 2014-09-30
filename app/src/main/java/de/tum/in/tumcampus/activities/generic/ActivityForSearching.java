package de.tum.in.tumcampus.activities.generic;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SearchRecentSuggestionsProvider;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.auxiliary.Utils;

/**
 * Generic class for searching. Provides basic functions for a {@link SearchView}
 * and typical processes related to search.
 * 
 */
public abstract class ActivityForSearching extends ProgressActivity {
    /** SearchView handle */
    private SearchView mSearchView;
    private MenuItem mSearchItem;

    /** @see ActivityForSearching#openSearch() */
    private boolean mOpenSearch;

    /** Search authority and minimum query length */
    private final String mAuthority;
    private final int mMinLength;

    /** Last search query */
    protected String mQuery = null;

    /**
     * Gets called if search has been canceled
     */
    protected abstract void onStartSearch();

    /**
     * Gets called if a search query has been entered
     * @param query Query to search for
     */
    protected abstract void onStartSearch(String query);

    /**
     * Initializes an activity for searching.
     * The xml layout must include an error_layout and a progress_layout.
     * A {@link uk.co.senab.actionbarpulltorefresh.extras.actionbarcompat.PullToRefreshLayout}
     * called ptr_layout is required if the activity should support PullToRefresh method
     *
     * @param layoutId Resource id of the xml layout that should be used to inflate the activity
     * @param auth Authority for search suggestions declared in manifest file
     * @param minLen Minimum text length that has to be entered by the user before a search quest can be submitted
     */
    public ActivityForSearching(int layoutId, String auth, int minLen) {
		super(layoutId);
        mAuthority = auth;
        mMinLength = minLen;
	}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setIcon(R.drawable.tum_logo);
        onNewIntent(getIntent());
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		// Inflate the menu; this adds a SearchView to the ActionBar
		getMenuInflater().inflate(R.menu.menu_search, menu);

        // Get SearchView
        mSearchItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) MenuItemCompat.getActionView(mSearchItem);

        // Set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchableInfo info = searchManager.getSearchableInfo(getComponentName());
        mSearchView.setSearchableInfo(info);

        // Optical tweaks to match application theme
        styleSearchView(info.getHintId());

        // If activity gets called via Intent with a search query set SearchView accordingly
        if(mQuery!=null) {
            mSearchView.setQuery(mQuery, false);
            mOpenSearch = true;
        }

        // Ensures that SearchView is updated if suggestion has been clicked
        mSearchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int i) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                String suggestion = getSuggestion(position);
                mSearchView.setQuery(suggestion, true);
                return true;
            }

            private String getSuggestion(int position) {
                Cursor cursor = (Cursor) mSearchView.getSuggestionsAdapter().getItem(position);
                return cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1));
            }
        });

        // Handle search cancellation
        mSearchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                MenuItemCompat.collapseActionView(mSearchItem);
                mQuery = null;
                onStartSearch();
                return false;
            }
        });
		return true;
	}

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if(mOpenSearch) {
            MenuItemCompat.setShowAsAction(mSearchItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS|MenuItemCompat.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
            MenuItemCompat.expandActionView(mSearchItem);
            return true;
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(intent==null)
            return;
        setIntent(intent);
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            requestSearch(intent.getStringExtra(SearchManager.QUERY));
        }
    }

    /**
     * Make some optical adjustments to SearchView to match blue actionbar color
     * @param hintId Resource id of the hint text to be shown in empty SearchView
     */
    private void styleSearchView(int hintId) {
        // Adjust small lense icon and hint text
        SearchView.SearchAutoComplete searchAutoComplete = (SearchView.SearchAutoComplete)mSearchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        SpannableStringBuilder ssb = new SpannableStringBuilder("   ");
        ssb.append(getString(hintId));
        Drawable searchIcon = getResources().getDrawable(R.drawable.ic_action_search);
        int textSize = (int) (searchAutoComplete.getTextSize() * 1.4);
        searchIcon.setBounds(0, 0, textSize, textSize);
        ssb.setSpan(new ImageSpan(searchIcon), 1, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        searchAutoComplete.setHint(ssb);
        searchAutoComplete.setHintTextColor(getResources().getColor(R.color.searchview_hint_color));
        searchAutoComplete.setTextColor(Color.WHITE);
        searchAutoComplete.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI|EditorInfo.IME_ACTION_SEARCH);

        // Adjust the color of the border shown below the EditText
        View searchPlate = mSearchView.findViewById(android.support.v7.appcompat.R.id.search_plate);
        searchPlate.setBackgroundResource(R.drawable.searchview_textfield_holo_blue);

        // Set a new x symbol
        ImageView searchCloseIcon = (ImageView)mSearchView.findViewById(android.support.v7.appcompat.R.id.search_close_btn);
        searchCloseIcon.setImageResource(R.drawable.ic_action_cancel);

        // Replace collapsed icon
        ImageView collapsedSearchIcon = (ImageView) mSearchView.findViewById(android.support.v7.appcompat.R.id.search_button);
        Drawable bigSearchIcon = getResources().getDrawable(R.drawable.ic_action_search);
        collapsedSearchIcon.setImageDrawable(bigSearchIcon);
    }

    /**
     * Tests if search query is valid and internet connection is available.
     * Then starts a new search.
     * @param query Query to search for
     */
	private void requestSearch(String query) {
        mQuery = query;
        if(query.length()<mMinLength) {
            final String text = String.format(getString(R.string.min_search_len), mMinLength);
            Utils.showToast(this, text);
            return;
        }

		if (!Utils.isConnected(this)) {
            showError(R.string.no_internet_connection);
			return;
		}

        // Add query to recents
        SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this, mAuthority, SearchRecentSuggestionsProvider.DATABASE_MODE_QUERIES);
        suggestions.saveRecentQuery(query, null);

        // Tell activity to start searching
        onStartSearch(query);
	}

    /**
     * Expands the Search-ActionView on activity startup, so that the user can immediately start typing.
     * Only has an effect if it is called during
     * {@link de.tum.in.tumcampus.activities.generic.ActivityForSearching#onCreate(android.os.Bundle)}} or
     * {@link de.tum.in.tumcampus.activities.generic.ActivityForSearching#onResume()}} method.
     */
    protected void openSearch() {
        mOpenSearch = true;
    }

    @Override
    public void onRefreshStarted(View view) {
        requestSearch(mQuery);
    }
}
