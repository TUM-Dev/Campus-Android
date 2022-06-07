package de.tum.`in`.tumcampusapp.component.ui.search

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.lifecycle.ViewModelProvider
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.generic.fragment.BaseFragment
import de.tum.`in`.tumcampusapp.di.ViewModelFactory
import de.tum.`in`.tumcampusapp.di.injector
import de.tum.`in`.tumcampusapp.utils.observeNonNull
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.toolbar_search.*
import javax.inject.Inject
import javax.inject.Provider

class SearchFragment: BaseFragment<Unit>(
        R.layout.fragment_search,
        R.string.search
) {

    @Inject
    lateinit var viewModelProvider: Provider<SearchViewModel>

    private val viewModel: SearchViewModel by lazy {
        val factory = ViewModelFactory(viewModelProvider)
        ViewModelProvider(this, factory).get(SearchViewModel::class.java)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        injector.searchComponent().inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val searchResultsAdapter = SearchResultsAdapter { searchResult ->
            onSearchResultClicked(searchResult) }
        searchResultsRecyclerView.adapter = searchResultsAdapter
        viewModel.searchResultList.observeNonNull(viewLifecycleOwner) { searchResults ->
            searchResultsAdapter.submitList(searchResults) }

        searchEditText.setOnEditorActionListener { textView, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val input = textView.text.toString().trim()
                viewModel.search(input)
                true
            } else {
                false
            }
        }
        searchEditText.requestFocus()
    }

    private fun onSearchResultClicked(searchResult: SearchResult) {
        println(searchResult)
    }
}