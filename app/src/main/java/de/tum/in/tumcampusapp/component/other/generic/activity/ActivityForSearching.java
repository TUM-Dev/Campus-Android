package de.tum.in.tumcampusapp.component.other.generic.activity;

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
import de.tum.in.tumcampusapp.utils.Utils;

/**
 * Generic class for searching. Provides basic functions for a {@link SearchView}
 * and typical processes related to search.
 */
public abstract class ActivityForSearching extends ProgressActivity {
    /**
     * Search authority and minimum query length
     */
    private final String authority;
    private final int minLength;
    /**
     * Last search query
     */
    protected String query;
    /**
     * SearchView handle
     */
    private SearchView searchView;
    private MenuItem searchItem;
    /**
     * @see ActivityForSearching#openSearch()
     */
    private boolean openSearch;

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
        authority = auth;
        minLength = minLen;
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
        getMenuInflater().inflate(R.menu.menu_search, menu);

        searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchItem.getActionView();

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        if (searchManager != null) {
            SearchableInfo info = searchManager.getSearchableInfo(getComponentName());
            searchView.setSearchableInfo(info);
        }

        // If activity gets called via Intent with a search query set SearchView accordingly
        if (query != null) {
            searchView.setQuery(query, false);
        }

        // Ensures that SearchView is updated if suggestion has been clicked
        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int i) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                String suggestion = getSuggestion(position);
                searchView.setQuery(suggestion, true);
                return true;
            }

            private String getSuggestion(int position) {
                Cursor cursor = (Cursor) searchView.getSuggestionsAdapter().getItem(position);
                return cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1));
            }
        });

        // Handle search cancellation
        searchView.setOnCloseListener(() -> {
            searchItem.collapseActionView();
            query = null;
            enableDrawer(true);
            onStartSearch();
            return false;
        });

        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                enableDrawer(false);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                query = null;
                onStartSearch();
                enableDrawer(true);
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (openSearch) {
            searchItem.setShowAsAction(
                    MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
            searchItem.expandActionView();
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
        this.query = query;
        if (query.length() < minLength) {
            final String text = String.format(getString(R.string.min_search_len), minLength);
            Utils.showToast(this, text);
            return;
        }

        // Add query to recents
        SearchRecentSuggestions suggestions = new SearchRecentSuggestions(
                this, authority, SearchRecentSuggestionsProvider.DATABASE_MODE_QUERIES);
        suggestions.saveRecentQuery(query, null);

        // Tell activity to start searching
        onStartSearch(query);
    }

    /**
     * Expands the Search-ActionView on activity startup, so that the user can immediately start typing.
     * Only has an effect if it is called during
     * {@link ActivityForSearching#onCreate(android.os.Bundle)}} or
     * {@link ActivityForSearching#onResume()}} method.
     */
    protected void openSearch() {
        openSearch = true;
    }

    @Override
    public void onRefresh() {
        String input = (query != null) ? query : "";
        requestSearch(input);
    }

}
