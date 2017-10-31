package de.tum.in.tumcampusapp.activities.generic;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SearchRecentSuggestionsProvider;
import android.database.Cursor;
import android.provider.SearchRecentSuggestions;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.auxiliary.Utils;

/**
 * Generic class for searching. Provides basic functions for a {@link SearchView}
 * and typical processes related to search.
 */
public abstract class ActivityForSearching extends ProgressActivity {
    /**
     * Search authority and minimum query length
     */
    private final String mAuthority;
    private final int mMinLength;
    /**
     * Last search query
     */
    protected String mQuery;
    /**
     * SearchView handle
     */
    private SearchView mSearchView;
    private MenuItem mSearchItem;
    /**
     * @see ActivityForSearching#openSearch()
     */
    private boolean mOpenSearch;

    /**
     * Initializes an activity for searching.
     * The xml layout must include an error_layout and a progress_layout.
     * A {@link SwipeRefreshLayout}
     * called ptr_layout is required if the activity should support PullToRefresh method
     *
     * @param layoutId Resource id of the xml layout that should be used to inflate the activity
     * @param auth     Authority for search suggestions declared in manifest file
     * @param minLen   Minimum text length that has to be entered by the user before a search quest can be submitted
     */
    public ActivityForSearching(int layoutId, String auth, int minLen) {
        super(layoutId);
        mAuthority = auth;
        mMinLength = minLen;
    }

    /**
     * Gets called if search has been canceled
     */
    protected abstract void onStartSearch();

    /**
     * Gets called if a search query has been entered
     *
     * @param query Query to search for
     */
    protected abstract void onStartSearch(String query);

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        // Inflate the menu; this adds a SearchView to the ActionBar
        getMenuInflater().inflate(R.menu.menu_search, menu);

        // Get SearchView
        mSearchItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) mSearchItem.getActionView();

        // Set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchableInfo info = searchManager.getSearchableInfo(getComponentName());
        mSearchView.setSearchableInfo(info);

        // If activity gets called via Intent with a search query set SearchView accordingly
        if (mQuery != null) {
            mSearchView.setQuery(mQuery, false);
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
                Cursor cursor = (Cursor) mSearchView.getSuggestionsAdapter()
                                                    .getItem(position);
                return cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1));
            }
        });

        // Handle search cancellation
        mSearchView.setOnCloseListener(() -> {
            mSearchItem.collapseActionView();
            mQuery = null;
            onStartSearch();
            return false;
        });

        mSearchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                mQuery = null;
                onStartSearch();
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mOpenSearch) {
            mSearchItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
            mSearchItem.expandActionView();
            return true;
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent == null) {
            return;
        }
        setIntent(intent);
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            requestSearch(intent.getStringExtra(SearchManager.QUERY));
        }
    }

    /**
     * Tests if search query is valid and internet connection is available.
     * Then starts a new search.
     *
     * @param query Query to search for
     */
    protected void requestSearch(String query) {
        mQuery = query;
        if (query.length() < mMinLength) {
            final String text = String.format(getString(R.string.min_search_len), mMinLength);
            Utils.showToast(this, text);
            return;
        }

        /*if (!Utils.isConnected(this)) {
            showNoInternetLayout();
            return;
        }*/

        // Add query to recents
        SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this, mAuthority, SearchRecentSuggestionsProvider.DATABASE_MODE_QUERIES);
        suggestions.saveRecentQuery(query, null);

        // Tell activity to start searching
        onStartSearch(query);
    }

    /**
     * Expands the Search-ActionView on activity startup, so that the user can immediately start typing.
     * Only has an effect if it is called during
     * {@link de.tum.in.tumcampusapp.activities.generic.ActivityForSearching#onCreate(android.os.Bundle)}} or
     * {@link de.tum.in.tumcampusapp.activities.generic.ActivityForSearching#onResume()}} method.
     */
    protected void openSearch() {
        mOpenSearch = true;
    }

    @Override
    public void onRefresh() {
        requestSearch(mQuery);
    }
}
