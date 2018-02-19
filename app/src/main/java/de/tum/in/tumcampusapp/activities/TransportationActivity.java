package de.tum.in.tumcampusapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.common.base.Optional;
import com.google.gson.Gson;

import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.ActivityForSearchingInBackground;
import de.tum.in.tumcampusapp.adapters.NoResultsAdapter;
import de.tum.in.tumcampusapp.auxiliary.MVVStationSuggestionProvider;
import de.tum.in.tumcampusapp.managers.RecentsManager;
import de.tum.in.tumcampusapp.managers.TransportManager;
import de.tum.in.tumcampusapp.models.efa.StationResult;

/**
 * Activity to show transport stations and departures
 */
public class TransportationActivity extends ActivityForSearchingInBackground<List<StationResult>> implements OnItemClickListener {

    private ListView listViewResults;
    private ArrayAdapter<StationResult> adapterStations;
    private RecentsManager recentsManager;
    private static final Gson gson = new Gson();

    public TransportationActivity() {
        super(R.layout.activity_transportation, MVVStationSuggestionProvider.AUTHORITY, 3);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get all stations from db
        recentsManager = new RecentsManager(this, RecentsManager.STATIONS);

        listViewResults = findViewById(R.id.activity_transport_listview_result);
        listViewResults.setOnItemClickListener(this);

        // Initialize stations adapter
        adapterStations = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, TransportManager.getRecentStations(recentsManager));

        if (adapterStations.getCount() == 0) {
            openSearch();
        } else {
            listViewResults.setAdapter(adapterStations);
            listViewResults.requestFocus();
        }
    }

    /**
     * Click on station in list
     */
    @Override
    public void onItemClick(final AdapterView<?> av, View v, int position, long id) {
        StationResult stationResult = (StationResult) av.getAdapter()
                                                        .getItem(position);
        showStation(stationResult);
    }

    /**
     * Opens {@link TransportationDetailsActivity} with departure times for the specified station
     *
     * @param stationResult the station to show
     */
    void showStation(StationResult stationResult) {
        Intent intent = new Intent(this, TransportationDetailsActivity.class);
        intent.putExtra(TransportationDetailsActivity.EXTRA_STATION, stationResult.getStation());
        intent.putExtra(TransportationDetailsActivity.EXTRA_STATION_ID, stationResult.getId());
        startActivity(intent);
    }

    /**
     * Shows all recently used stations
     *
     * @return List of StationResults
     */
    @Override
    public Optional<List<StationResult>> onSearchInBackground() {
        return Optional.of(TransportManager.getRecentStations(recentsManager));
    }

    /**
     * Searches the Webservice for stations
     *
     * @param query the text entered by the user
     * @return List of StationResult
     */
    @Override
    public Optional<List<StationResult>> onSearchInBackground(String query) {
        // Get Information
        List<StationResult> stations = TransportManager.getStationsFromExternal(this, query);

        // Drop results if canceled
        if (asyncTask.isCancelled()) {
            return Optional.absent();
        }

        return Optional.of(stations);
    }

    /**
     * Shows the stations
     *
     * @param possibleStationList List of possible stations
     */
    @Override
    protected void onSearchFinished(Optional<List<StationResult>> possibleStationList) {
        if (!possibleStationList.isPresent()) {
            return;
        }
        List<StationResult> stationList = possibleStationList.get();
        showLoadingEnded();

        // mQuery is not null if it was a real search
        // If there is exactly one station, open results directly
        if (stationList.size() == 1 && mQuery != null) {
            showStation(stationList.get(0));
            return;
        } else if (stationList.isEmpty()) {
            // So show no results found
            listViewResults.setAdapter(new NoResultsAdapter(this));
            listViewResults.requestFocus();
            return;
        }

        adapterStations.clear();
        adapterStations.addAll(stationList);

        adapterStations.notifyDataSetChanged();
        listViewResults.setAdapter(adapterStations);
        listViewResults.requestFocus();
    }
}