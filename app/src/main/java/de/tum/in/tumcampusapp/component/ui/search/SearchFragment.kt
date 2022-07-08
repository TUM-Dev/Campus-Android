package de.tum.`in`.tumcampusapp.component.ui.search

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.api.navigatum.domain.NavigationEntity
import de.tum.`in`.tumcampusapp.component.other.general.RecentsDao
import de.tum.`in`.tumcampusapp.component.other.general.model.Recent
import de.tum.`in`.tumcampusapp.component.other.generic.fragment.BaseFragment
import de.tum.`in`.tumcampusapp.component.tumui.lectures.activity.LectureDetailsActivity
import de.tum.`in`.tumcampusapp.component.tumui.lectures.model.Lecture
import de.tum.`in`.tumcampusapp.component.tumui.person.PersonDetailsActivity
import de.tum.`in`.tumcampusapp.component.tumui.person.PersonDetailsActivity.Companion.PERSON_OBJECT
import de.tum.`in`.tumcampusapp.component.tumui.person.model.Person
import de.tum.`in`.tumcampusapp.component.tumui.roomfinder.NavigationDetailsFragment
import de.tum.`in`.tumcampusapp.component.tumui.roomfinder.NavigationDetailsFragment.Companion.NAVIGATION_ENTITY
import de.tum.`in`.tumcampusapp.component.ui.search.adapter.RecentSearchesAdapter
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

class SearchFragment : BaseFragment<Unit>(
    R.layout.fragment_search,
    R.string.search
) {

    @Inject
    lateinit var viewModelProvider: Provider<SearchViewModel>

    private lateinit var searchResultsAdapter: SearchResultsAdapter
    private lateinit var resultTypesAdapter: ResultTypesAdapter
    private lateinit var recentSearchesAdapter: RecentSearchesAdapter

    private val viewModel: SearchViewModel by lazy {
        val factory = ViewModelFactory(viewModelProvider)
        ViewModelProvider(this, factory).get(SearchViewModel::class.java)
    }

    private val query: String? by lazy {
        arguments?.getString(SearchManager.QUERY)
    }

    private lateinit var resultLauncher: ActivityResultLauncher<Intent>

    override fun onAttach(context: Context) {
        super.onAttach(context)
        injector.searchComponent().inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == BACK_FROM_PERSON_BY_ROOM_CLICK) {
                handleBackFromPersonDetailsByRoomClick(result)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initSearchResultsAdapter()
        initSearchResultTypesAdapter()
        initRecentSearchesAdapter()

        lifecycleScope.launch {
            handleStateChange()
        }

        query?.let {
            viewModel.search(it)
            searchEditText.setText(query)
        } ?: run {
            showKeyboard()
        }

        viewModel.fetchRecentSearches(requireContext())

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

    private fun initRecentSearchesAdapter() {
        recentSearchesAdapter = RecentSearchesAdapter(
            onSelect = { recentSearch -> onRecentSearchSelected(recentSearch) },
            onRemove = { recentSearch -> removeRecentSearch(recentSearch) }
        )
        recentSearchesRecyclerView.adapter = recentSearchesAdapter

        clearRecentSearches.setOnClickListener {
            viewModel.clearRecentSearchesHistory(requireContext())
            showSearchInfo()
        }
    }

    private fun onRecentSearchSelected(recent: Recent) {
        try {
            when (recent.type) {
                RecentsDao.PERSONS -> openPersonDetails(Person.fromRecent(recent))
                RecentsDao.LECTURES -> openLectureDetails(Lecture.fromRecent(recent))
                RecentsDao.NAVIGA_TUM_BUILDINGS -> openNavigationDetails(NavigationEntity.fromRecent(recent))
                RecentsDao.NAVIGA_TUM_ROOMS -> openNavigationDetails(NavigationEntity.fromRecent(recent))
            }
        } catch (exception: Exception) {
            Utils.showToast(requireContext(), R.string.something_wrong)
        }
    }

    private fun removeRecentSearch(recentSearch: Recent) {
        viewModel.removeRecentSearch(recentSearch, requireContext())
        showSearchInfo()
    }

    private fun hideKeyboard() {
        val imm: InputMethodManager? = requireContext().getSystemService()
        imm?.hideSoftInputFromWindow(searchEditText.windowToken, 0)
    }

    private fun clearInput() {
        searchEditText.setText("")
        showSearchInfo()
        viewModel.clearSearchState()
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
                saveRecentSearch(Person.toRecent(searchResult.person))
                openPersonDetails(searchResult.person)
            }
            is SearchResult.Lecture -> {
                saveRecentSearch(Lecture.toRecent(searchResult.lecture))
                openLectureDetails(searchResult.lecture)
            }
            is SearchResult.Building -> {
                saveRecentSearch(NavigationEntity.toRecent(searchResult.building, RecentsDao.NAVIGA_TUM_BUILDINGS))
                openNavigationDetails(searchResult.building)
            }
            is SearchResult.NavigaRoom -> {
                saveRecentSearch(NavigationEntity.toRecent(searchResult.room, RecentsDao.NAVIGA_TUM_ROOMS))
                openNavigationDetails(searchResult.room)
            }
        }
    }

    private fun openNavigationDetails(navigationEntity: NavigationEntity) {
        val navigationFragment = NavigationDetailsFragment()
        val bundle = Bundle()
        bundle.putSerializable(NAVIGATION_ENTITY, navigationEntity)
        navigationFragment.arguments = bundle
        requireActivity().supportFragmentManager
            .beginTransaction()
            .replace(R.id.contentFrame, navigationFragment)
            .commitNow()
    }

    private fun openPersonDetails(person: Person) {
        val intent = Intent(requireContext(), PersonDetailsActivity::class.java).apply {
            putExtra(PERSON_OBJECT, person)
        }
        resultLauncher.launch(intent)
    }

    private fun handleBackFromPersonDetailsByRoomClick(result: ActivityResult) {
        result.data?.let {
            val queryString = it.getStringExtra(SearchManager.QUERY)
            queryString?.let { query ->
                hideKeyboard()
                viewModel.search(query)
                searchEditText.setText(query)
            }
        }
    }

    private fun openLectureDetails(lecture: Lecture) {
        val intent = Intent(requireContext(), LectureDetailsActivity::class.java)
        intent.putExtra(Lecture.STP_SP_NR, lecture.stp_sp_nr)
        startActivity(intent)
    }

    private fun saveRecentSearch(recent: Recent) {
        viewModel.saveRecentSearch(recent, requireContext())
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

            recentSearchesAdapter.submitList(searchResultState.recentSearches)
            searchResultsAdapter.submitList(searchResultState.data)

            if (searchResultState.isLoading) {
                progressIndicator.show()
            } else {
                progressIndicator.hide()

                val input = searchEditText.text.toString().trim()
                if (input.length < MIN_QUERY_LENGTH)
                    showSearchInfo()
                else if (viewModel.state.value.data.isEmpty()) {
                    showNoResultInfo(input)
                } else {
                    hideResultInfo()
                }
            }

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
        if (viewModel.state.value.recentSearches.isNotEmpty()) {
            noResultInfo.visibility = View.GONE
            recentSearchesLayout.visibility = View.VISIBLE
        } else {
            recentSearchesLayout.visibility = View.GONE
            noResultInfo.visibility = View.VISIBLE
            noResultInfo.infoTitle.setText(R.string.search_info)
            noResultInfo.infoSubtitle.text = ""
        }
    }

    private fun showNoResultInfo(input: String) {
        recentSearchesLayout.visibility = View.GONE
        noResultInfo.visibility = View.VISIBLE
        noResultInfo.infoTitle.text =
            String.format(resources.getString(R.string.no_result_info, input))
        noResultInfo.infoSubtitle.setText(R.string.no_result_sub_info)
    }

    private fun hideResultInfo() {
        noResultInfo.visibility = View.GONE
        recentSearchesLayout.visibility = View.GONE
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
        const val BACK_FROM_PERSON_BY_ROOM_CLICK = 123

        fun newInstance(query: String) = SearchFragment().apply {
            arguments = bundleOf(SearchManager.QUERY to query)
        }
    }
}
