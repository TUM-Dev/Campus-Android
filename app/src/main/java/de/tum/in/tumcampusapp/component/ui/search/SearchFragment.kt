package de.tum.`in`.tumcampusapp.component.ui.search

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
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
import de.tum.`in`.tumcampusapp.utils.Utils
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.fragment_search.view.*
import kotlinx.android.synthetic.main.toolbar_search.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
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

        showKeyboard()

        initSearchResultsAdapter()
        initSearchResultTypesAdapter()

        lifecycleScope.launch {
            handleStateChange()
        }

        searchEditText.setOnEditorActionListener { textView, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val input = textView.text.toString().trim()
                if (input.length < MIN_QUERY_LENGTH) {
                    val text = String.format(getString(R.string.min_search_len), MIN_QUERY_LENGTH)
                    Utils.showToast(requireContext(), text)
                    showSearchInfo()
                } else {
                    viewModel.search(input)
                    hideKeyboard()
                }
                true
            } else {
                false
            }
        }

        clearButton.setOnClickListener {
            clearInput()
        }
    }

    private fun hideKeyboard() {
        val imm: InputMethodManager? = requireContext().getSystemService()
        searchEditText.clearFocus()
        imm?.hideSoftInputFromWindow(searchEditText.windowToken, 0)
    }

    private fun clearInput() {
        searchEditText.setText("")
        showSearchInfo()
        viewModel.clearState()
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

            if (searchResultState.isLoading) {
                progressIndicator.show()
            } else {
                progressIndicator.hide()

                val input = searchEditText.text.toString().trim()
                if (input.length < MIN_QUERY_LENGTH)
                    showSearchInfo()
                else if (viewModel.state.value.data.isEmpty()){
                    showNoResultInfo(input)
                } else {
                    hideResultInfo()
                }
            }

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

    private fun showKeyboard() {
        val imm: InputMethodManager? = requireContext().getSystemService()
        searchEditText.requestFocus()
        imm?.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun showSearchInfo() {
        noResultInfo.visibility = View.VISIBLE
        noResultInfo.infoTitle.setText(R.string.search_info)
        noResultInfo.infoSubtitle.text = ""
    }

    private fun showNoResultInfo(input: String) {
        noResultInfo.visibility = View.VISIBLE
        noResultInfo.infoTitle.text =
                String.format(resources.getString(R.string.no_result_info, input))
        noResultInfo.infoSubtitle.setText(R.string.no_result_sub_info)
    }

    private fun hideResultInfo() {
        noResultInfo.visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()
        initBackButton()
    }

    private fun initBackButton() {
        val backIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_arrow_back)
        backIcon?.let {
            val color = ContextCompat.getColor(requireContext(), R.color.tum_500)
            it.setTint(color)
        }

        toolbar.navigationIcon = backIcon
        toolbar.setNavigationOnClickListener {
            hideKeyboard()
            requireActivity().onBackPressed()
        }
    }

    companion object {
        private const val MIN_QUERY_LENGTH = 3
    }
}