package de.tum.`in`.tumcampusapp.component.ui.search

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.generic.fragment.BaseFragment
import de.tum.`in`.tumcampusapp.component.tumui.lectures.activity.LectureDetailsActivity
import de.tum.`in`.tumcampusapp.component.tumui.lectures.model.Lecture
import de.tum.`in`.tumcampusapp.component.tumui.person.PersonDetailsActivity
import de.tum.`in`.tumcampusapp.component.tumui.roomfinder.RoomFinderDetailsActivity
import de.tum.`in`.tumcampusapp.component.ui.search.adapter.ResultTypeData
import de.tum.`in`.tumcampusapp.component.ui.search.adapter.ResultTypesAdapter
import de.tum.`in`.tumcampusapp.component.ui.search.adapter.SearchResultsAdapter
import de.tum.`in`.tumcampusapp.di.ViewModelFactory
import de.tum.`in`.tumcampusapp.di.injector
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.toolbar_search.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect
import javax.inject.Inject
import javax.inject.Provider

class SearchFragment: BaseFragment<Unit>(
        R.layout.fragment_search,
        R.string.search
) {

    @Inject
    lateinit var viewModelProvider: Provider<SearchViewModel>

    private lateinit var searchResultsAdapter: SearchResultsAdapter
    private lateinit var resultTypesAdapter: ResultTypesAdapter

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

        initSearchResultsAdapter()
        initSearchResultTypesAdapter()

        lifecycleScope.launch {
            handleStateChange()
        }

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

    private fun initSearchResultsAdapter() {
        searchResultsAdapter = SearchResultsAdapter { onSearchResultClicked(it) }
        searchResultsRecyclerView.adapter = searchResultsAdapter

        searchResultsAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                searchResultsRecyclerView.scrollToPosition(0)
            }

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                searchResultsRecyclerView.scrollToPosition(0)
            }

            override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
                searchResultsRecyclerView.scrollToPosition(0)
            }

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                searchResultsRecyclerView.scrollToPosition(0)
            }

            override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
                searchResultsRecyclerView.scrollToPosition(0)
            }

            override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
                searchResultsRecyclerView.scrollToPosition(0)
            }
        })
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

    private fun initSearchResultTypesAdapter() {
        resultTypesAdapter = ResultTypesAdapter(
                onClick = { onResultTypeClicked(it.type) }
        )
        searchResultTypesRecyclerView.adapter = resultTypesAdapter
    }

    private fun onResultTypeClicked(type: SearchResultType) {
        viewModel.changeResultType(type)
    }

    private suspend fun handleStateChange() {
        viewModel.state.collect { searchResultState ->
            searchResultsAdapter.submitList(searchResultState.data)
            if (searchResultState.availableResultTypes.isNotEmpty()) {
                searchResultTypesRecyclerView.visibility = View.VISIBLE
                resultTypesAdapter.submitList(mapToResultTypeData(viewModel.state.value.availableResultTypes, viewModel.state.value.selectedType))
            } else {
                searchResultTypesRecyclerView.visibility = View.GONE
                resultTypesAdapter.submitList(emptyList())
            }
        }
    }

    private fun mapToResultTypeData(
            resultTypeList: List<SearchResultType>,
            selectedType: SearchResultType
    ): List<ResultTypeData> {
        val availableTypes = listOf(SearchResultType.ALL) + resultTypeList
        return availableTypes.map { searchResultType ->
            ResultTypeData(
                    type = searchResultType,
                    selectedType = selectedType
            )
        }
    }
}