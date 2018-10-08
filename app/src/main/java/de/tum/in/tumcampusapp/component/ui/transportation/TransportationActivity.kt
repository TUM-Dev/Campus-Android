package de.tum.`in`.tumcampusapp.component.ui.transportation

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.ListView
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.general.RecentsDao
import de.tum.`in`.tumcampusapp.component.other.generic.activity.ActivityForSearching
import de.tum.`in`.tumcampusapp.component.other.generic.adapter.NoResultsAdapter
import de.tum.`in`.tumcampusapp.component.ui.transportation.model.efa.StationResult
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.utils.Utils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.net.UnknownHostException

/**
 * Activity to show transport stations and departures
 */
class TransportationActivity : ActivityForSearching(
        R.layout.activity_transportation,
        MVVStationSuggestionProvider.AUTHORITY, 3
), OnItemClickListener {

    private lateinit var listViewResults: ListView
    private lateinit var adapterStations: ArrayAdapter<StationResult>
    private lateinit var recentsDao: RecentsDao

    private val disposable = CompositeDisposable()

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        recentsDao = TcaDb.getInstance(this)
                .recentsDao()

        listViewResults = findViewById(R.id.activity_transport_listview_result)
        listViewResults.onItemClickListener = this

        // Initialize stations adapter
        val recentStations = recentsDao.getAll(RecentsDao.STATIONS) ?: emptyList()
        adapterStations = ArrayAdapter(this, android.R.layout.simple_list_item_1,
                TransportController.getRecentStations(recentStations))

        if (adapterStations.count == 0) {
            openSearch()
            return
        }

        listViewResults.adapter = adapterStations

    }

    /**
     * Click on station in list
     */
    override fun onItemClick(av: AdapterView<*>, v: View, position: Int, id: Long) {
        val stationResult = av.adapter.getItem(position) as StationResult
        transitionToDetailsActivity(stationResult)
    }

    /**
     * Opens [TransportationDetailsActivity] with departure times for the specified station
     *
     * @param stationResult the station to show
     */
    private fun transitionToDetailsActivity(stationResult: StationResult) {
        val intent = Intent(this, TransportationDetailsActivity::class.java)
        intent.putExtra(TransportationDetailsActivity.EXTRA_STATION, stationResult.station)
        intent.putExtra(TransportationDetailsActivity.EXTRA_STATION_ID, stationResult.id)
        startActivity(intent)
    }

    override fun onStartSearch() {
        val recents = recentsDao.getAll(RecentsDao.STATIONS)
        if (recents == null) {
            listViewResults.adapter = NoResultsAdapter(this)
            return
        }

        val stations = TransportController.getRecentStations(recents)
        displayStations(stations)
    }

    override fun onStartSearch(query: String) {
        disposable.add(TransportController.getStationsFromExternal(this, query)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::displayStations) { t ->
                    // Something went wrong
                    Utils.log(t)
                    when (t) {
                        is UnknownHostException -> showNoInternetLayout()
                        else -> showError(R.string.something_wrong)
                    }
                })
    }

    private fun displayStations(stations: List<StationResult>) {
        showLoadingEnded()

        if (stations.isEmpty()) {
            showEmptyResponseLayout(R.string.no_search_result)
            return
        }

        // query is not null if it was a real search
        // If there is exactly one station, open results directly
        if (stations.size == 1 && query != null) {
            transitionToDetailsActivity(stations[0])
            return
        }

        adapterStations.clear()
        adapterStations.addAll(stations)

        adapterStations.notifyDataSetChanged()
        listViewResults.adapter = adapterStations
    }

    override fun onStop() {
        super.onStop()
        disposable.clear()
    }
}