package de.tum.in.tumcampusapp.activities;

import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.common.base.Optional;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.ActivityForSearchingInBackground;
import de.tum.in.tumcampusapp.adapters.NoResultsAdapter;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.MVVStationSuggestionProvider;
import de.tum.in.tumcampusapp.managers.RecentsManager;
import de.tum.in.tumcampusapp.managers.TransportManager;
import de.tum.in.tumcampusapp.models.efa.StationResult;

/**
 * Activity to show transport stations and departures
 */
public class TransportationActivity extends ActivityForSearchingInBackground<Cursor> implements OnItemClickListener {

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
        try (Cursor stationCursor = recentsManager.getAllFromDb()) {
            adapterStations = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, getAllStationResults(stationCursor));
        }

        if (adapterStations.getCount() == 0) {
            openSearch();
        } else {
            listViewResults.setAdapter(adapterStations);
            listViewResults.requestFocus();
        }
    }

    public static List<StationResult> getAllStationResults(Cursor stationCursor) {
        List<StationResult> stationResults = new ArrayList<>(stationCursor.getCount());
        if (stationCursor.moveToFirst()) {
            do {
                String jsonStationResult = stationCursor.getString(stationCursor.getColumnIndex(Const.NAME_COLUMN));
                StationResult stationResult = gson.fromJson(jsonStationResult, StationResult.class);
                stationResults.add(stationResult);
            } while (stationCursor.moveToNext());
        }
        return stationResults;
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
     * @return Cursor holding the recents information (name, _id)
     */
    @Override
    public Optional<Cursor> onSearchInBackground() {
        return Optional.of(recentsManager.getAllFromDb());
    }

    /**
     * Searches the Webservice for stations
     *
     * @param query the text entered by the user
     * @return Cursor holding the stations (name, _id)
     */
    @Override
    public Optional<Cursor> onSearchInBackground(String query) {
        // Get Information
        Optional<Cursor> stationCursor = TransportManager.getStationsFromExternal(this, query);
        if (!stationCursor.isPresent()) {
            showError(R.string.exception_unknown);
        }

        // Drop results if canceled
        if (asyncTask.isCancelled()) {
            return Optional.absent();
        }

        return stationCursor;
    }

    /**
     * Shows the stations
     *
     * @param possibleStationCursor Cursor with stations (name, _id)
     */
    @Override
    protected void onSearchFinished(Optional<Cursor> possibleStationCursor) {
        if (!possibleStationCursor.isPresent()) {
            return;
        }
        try (Cursor stationCursor = possibleStationCursor.get()) {

            showLoadingEnded();

            // mQuery is not null if it was a real search
            // If there is exactly one station, open results directly
            if (stationCursor.getCount() == 1 && mQuery != null) {
                StationResult stationResult = getAllStationResults(stationCursor).get(0);
                showStation(stationResult);
                return;
            } else if (stationCursor.getCount() == 0) {
                // When stationCursor is a MatrixCursor the result comes from querying a station name
                if (stationCursor instanceof MatrixCursor) {
                    // So show no results found
                    listViewResults.setAdapter(new NoResultsAdapter(this));
                    listViewResults.requestFocus();
                } else {
                    // if the loading came from the user canceling search
                    // and there are no recents to show close activity
                    finish();
                }
                return;
            }

            adapterStations.clear();
            adapterStations.addAll(getAllStationResults(stationCursor));
        }
        adapterStations.notifyDataSetChanged();
        listViewResults.setAdapter(adapterStations);
        listViewResults.requestFocus();
    }
}