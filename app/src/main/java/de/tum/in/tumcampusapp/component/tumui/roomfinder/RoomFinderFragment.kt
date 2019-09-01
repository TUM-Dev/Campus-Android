package de.tum.`in`.tumcampusapp.component.tumui.roomfinder

import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.component.other.general.RecentsDao
import de.tum.`in`.tumcampusapp.component.other.general.model.Recent
import de.tum.`in`.tumcampusapp.component.other.generic.adapter.NoResultsAdapter
import de.tum.`in`.tumcampusapp.component.other.generic.fragment.FragmentForSearchingInBackground
import de.tum.`in`.tumcampusapp.component.tumui.roomfinder.model.RoomFinderRoom
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.utils.NetUtils
import de.tum.`in`.tumcampusapp.utils.Utils
import kotlinx.android.synthetic.main.fragment_roomfinder.listView
import java.io.IOException
import java.io.Serializable
import java.util.regex.Pattern

class RoomFinderFragment : FragmentForSearchingInBackground<List<RoomFinderRoom>>(
    R.layout.fragment_roomfinder,
    R.string.roomfinder,
    RoomFinderSuggestionProvider.AUTHORITY,
    minLen = 3
) {

    private val recentsDao by lazy { TcaDb.getInstance(requireContext()).recentsDao() }
    private lateinit var adapter: RoomFinderListAdapter

    private val recents: List<RoomFinderRoom>
        get() {
            return recentsDao.getAll(RecentsDao.ROOMS)?.mapNotNull {
                try {
                    RoomFinderRoom.fromRecent(it)
                } catch (ignore: IllegalArgumentException) {
                    null
                }
            }.orEmpty()
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = RoomFinderListAdapter(requireContext(), recents)

        listView.setOnItemClickListener { _, _, position, _ ->
            val room = listView.adapter.getItem(position) as RoomFinderRoom
            openRoomDetails(room)
        }

        val intent = requireActivity().intent
        if (intent != null && intent.hasExtra(SearchManager.QUERY)) {
            val query = checkNotNull(intent.getStringExtra(SearchManager.QUERY))
            requestSearch(query)
            return
        }

        if (adapter.isEmpty) {
            openSearch()
        } else {
            listView.adapter = adapter
        }
    }

    override fun onSearchInBackground(): List<RoomFinderRoom>? = recents

    override fun onSearchInBackground(query: String): List<RoomFinderRoom>? {
        return try {
            TUMCabeClient
                .getInstance(requireContext())
                .fetchRooms(userRoomSearchMatching(query))
        } catch (e: IOException) {
            Utils.log(e)
            null
        }
    }

    override fun onSearchFinished(result: List<RoomFinderRoom>?) {
        if (result == null) {
            if (NetUtils.isConnected(requireContext())) {
                showErrorLayout()
            } else {
                showNoInternetLayout()
            }
            return
        }

        if (result.isEmpty()) {
            listView.adapter = NoResultsAdapter(requireContext())
        } else {
            adapter = RoomFinderListAdapter(requireContext(), result)
            listView.adapter = adapter
        }
        showLoadingEnded()
    }

    /**
     * Opens a [RoomFinderDetailsActivity] that displays details (e.g. location on a map) for
     * a given room. Also adds this room to the recent queries.
     */
    private fun openRoomDetails(room: Serializable) {
        recentsDao.insert(Recent(room.toString(), RecentsDao.ROOMS))

        // Start detail activity
        val intent = Intent(requireContext(), RoomFinderDetailsActivity::class.java)
        intent.putExtra(RoomFinderDetailsActivity.EXTRA_ROOM_INFO, room)
        startActivity(intent)
    }

    /**
     * Distinguishes between some room searches, eg. MW 2001 or MI 01.15.069 and takes the
     * number part so that the search can return (somewhat) meaningful results
     * (Temporary and non-optimal)
     *
     * @return a new query or the original one if nothing was matched
     */
    private fun userRoomSearchMatching(roomSearchQuery: String): String {
        // Matches the number part if the String is composed of two words, probably wrong:

        // First group captures numbers with dots, like the 01.15.069 part from 'MI 01.15.069'
        // (This is the best search format for MI room numbers)
        // The second group captures numbers and mixed formats with letters, like 'MW2001'
        // Only the first match will be returned
        val pattern = Pattern.compile("(\\w+(?:\\.\\w+)+)|(\\w+\\d+)")

        val matcher = pattern.matcher(roomSearchQuery)

        return if (matcher.find()) {
            matcher.group()
        } else {
            roomSearchQuery
        }
    }

    companion object {
        fun newInstance() = RoomFinderFragment()
    }
}
