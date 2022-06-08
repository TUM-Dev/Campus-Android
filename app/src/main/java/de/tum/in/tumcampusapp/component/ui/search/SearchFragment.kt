package de.tum.`in`.tumcampusapp.component.ui.search

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.lifecycle.ViewModelProvider
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.generic.fragment.BaseFragment
import de.tum.`in`.tumcampusapp.component.tumui.lectures.activity.LectureDetailsActivity
import de.tum.`in`.tumcampusapp.component.tumui.lectures.model.Lecture
import de.tum.`in`.tumcampusapp.component.tumui.person.PersonDetailsActivity
import de.tum.`in`.tumcampusapp.component.tumui.roomfinder.RoomFinderDetailsActivity
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

        val searchResultsAdapter = SearchResultsAdapter { onSearchResultClicked(it) }
        searchResultsRecyclerView.adapter = searchResultsAdapter
        viewModel.searchResultList.observeNonNull(viewLifecycleOwner) { searchResultsAdapter.submitList(it) }

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
        when (searchResult) {
            is SearchResult.Person -> {
                val intent = Intent(requireContext(), PersonDetailsActivity::class.java).apply {
                    putExtra("personObject", searchResult.person)
                }
                println(searchResult.person)
                startActivity(intent)
            }
            is SearchResult.Room -> {
                val intent = Intent(requireContext(), RoomFinderDetailsActivity::class.java)
                intent.putExtra(RoomFinderDetailsActivity.EXTRA_ROOM_INFO, searchResult.room)
                startActivity(intent)
            }
            is SearchResult.Lecture -> {
                val intent = Intent(requireContext(), LectureDetailsActivity::class.java)
                intent.putExtra(Lecture.STP_SP_NR, searchResult.lecture.stp_sp_nr)
                startActivity(intent)
            }
        }

    }
}