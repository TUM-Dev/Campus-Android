package de.tum.`in`.tumcampusapp.component.tumui.lectures.fragment

import android.content.Intent
import android.os.Bundle
import android.view.View
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.api.tumonline.CacheControl
import de.tum.`in`.tumcampusapp.component.other.generic.adapter.NoResultsAdapter
import de.tum.`in`.tumcampusapp.component.other.generic.fragment.FragmentForSearchingTumOnline
import de.tum.`in`.tumcampusapp.component.tumui.lectures.LectureSearchSuggestionProvider
import de.tum.`in`.tumcampusapp.component.tumui.lectures.activity.LecturesDetailsActivity
import de.tum.`in`.tumcampusapp.component.tumui.lectures.adapter.LecturesListAdapter
import de.tum.`in`.tumcampusapp.component.tumui.lectures.model.Lecture
import de.tum.`in`.tumcampusapp.component.tumui.lectures.model.LecturesResponse
import kotlinx.android.synthetic.main.fragment_lectures.*

class LecturesFragment : FragmentForSearchingTumOnline<LecturesResponse>(
    R.layout.fragment_lectures,
    R.string.my_lectures,
    authority = LectureSearchSuggestionProvider.AUTHORITY,
    minLength = 4
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lecturesListView.setOnItemClickListener { _, _, position, _ ->
            val item = lecturesListView.getItemAtPosition(position) as Lecture
            val intent = Intent(requireContext(), LecturesDetailsActivity::class.java)
            intent.putExtra(Lecture.STP_SP_NR, item.stp_sp_nr)
            startActivity(intent)
        }

        onStartSearch()
    }

    override fun onRefresh() {
        loadPersonalLectures(CacheControl.BYPASS_CACHE)
    }

    override fun onStartSearch() {
        enableRefresh()
        loadPersonalLectures(CacheControl.USE_CACHE)
    }

    override fun onStartSearch(query: String?) {
        query?.let {
            disableRefresh()
            searchLectures(query)
        }
    }

    private fun loadPersonalLectures(cacheControl: CacheControl) {
        val apiCall = apiClient.getPersonalLectures(cacheControl)
        fetch(apiCall)
    }

    private fun searchLectures(query: String) {
        val apiCall = apiClient.searchLectures(query)
        fetch(apiCall)
    }

    override fun onDownloadSuccessful(response: LecturesResponse) {
        if (response.lectures.isEmpty()) {
            lecturesListView.adapter = NoResultsAdapter(requireContext())
        } else {
            val lectures = response.lectures.sorted()
            lecturesListView.adapter = LecturesListAdapter(requireContext(), lectures.toMutableList())
        }
    }

    companion object {
        @JvmStatic fun newInstance() = LecturesFragment()
    }

}
