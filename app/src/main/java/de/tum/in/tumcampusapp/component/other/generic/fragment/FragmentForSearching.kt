package de.tum.`in`.tumcampusapp.component.other.generic.fragment

import android.app.SearchManager
import android.content.SearchRecentSuggestionsProvider.DATABASE_MODE_QUERIES
import android.database.Cursor
import android.os.Bundle
import android.provider.SearchRecentSuggestions
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW
import android.view.MenuItem.SHOW_AS_ACTION_IF_ROOM
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.SearchView
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.generic.activity.BaseNavigationActivity
import de.tum.`in`.tumcampusapp.utils.Utils
import org.jetbrains.anko.searchManager

abstract class FragmentForSearching<T>(
    @LayoutRes layoutId: Int,
    @StringRes titleResId: Int,
    private val authority: String,
    private val minLength: Int
) : BaseFragment<T>(layoutId, titleResId) {

    /**
     * Last search query
     */
    protected var query: String? = null

    /**
     * SearchView handle
     */
    private lateinit var searchView: SearchView
    private lateinit var searchItem: MenuItem

    private var openSearch: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    protected abstract fun onStartSearch()

    protected abstract fun onStartSearch(query: String?)

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)
        if (openSearch) {
            searchItem.setShowAsAction(SHOW_AS_ACTION_IF_ROOM or SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW)
            searchItem.expandActionView()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_search, menu)

        searchItem = checkNotNull(menu?.findItem(R.id.action_search))
        searchView = searchItem.actionView as SearchView

        val searchManager = requireContext().searchManager
        val info = searchManager.getSearchableInfo(requireActivity().componentName)
        searchView.setSearchableInfo(info)

        // If activity gets called via Intent with a search query set SearchView accordingly
        query?.let {
            searchView.setQuery(it, false)
        }

        // Ensures that SearchView is updated if suggestion has been clicked
        searchView.setOnSuggestionListener(object : SearchView.OnSuggestionListener {
            override fun onSuggestionSelect(i: Int): Boolean = false

            override fun onSuggestionClick(position: Int): Boolean {
                val suggestion = getSuggestion(position)
                searchView.setQuery(suggestion, true)
                return true
            }

            private fun getSuggestion(position: Int): String {
                val cursor = searchView.suggestionsAdapter.getItem(position) as Cursor
                return cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1))
            }
        })

        // Handle search cancellation
        searchView.setOnCloseListener {
            searchItem.collapseActionView()
            query = null
            val activity = requireActivity() as BaseNavigationActivity
            activity.enableDrawer(false)
            onStartSearch()
            false
        }

        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                val activity = requireActivity() as BaseNavigationActivity
                activity.enableDrawer(false)
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                query = null
                onStartSearch()
                val activity = requireActivity() as BaseNavigationActivity
                activity.enableDrawer(true)
                return true
            }
        })

        super.onCreateOptionsMenu(menu, inflater)
    }

    /**
     * Tests if search query is valid and internet connection is available.
     * Then starts a new search.
     *
     * @param query Query to search for
     */
    protected fun requestSearch(query: String) {
        this.query = query
        if (query.length < minLength) {
            val text = String.format(getString(R.string.min_search_len), minLength)
            Utils.showToast(requireContext(), text)
            return
        }

        // Add query to recents
        val suggestions = SearchRecentSuggestions(requireContext(), authority, DATABASE_MODE_QUERIES)
        suggestions.saveRecentQuery(query, null)

        // Tell activity to start searching
        onStartSearch(query)
    }

    /**
     * Expands the search ActionView on fragment creation, so that the user can immediately start
     * typing.
     */
    protected fun openSearch() {
        openSearch = true
    }

    override fun onRefresh() {
        requestSearch(query.orEmpty())
    }

}
