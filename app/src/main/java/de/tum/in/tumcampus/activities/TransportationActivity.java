package de.tum.in.tumcampus.activities;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import java.util.NoSuchElementException;
import java.util.concurrent.TimeoutException;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.generic.ActivityForSearchingInBackground;
import de.tum.in.tumcampus.adapters.NoResultsAdapter;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.MVVStationSuggestionProvider;
import de.tum.in.tumcampus.models.managers.RecentsManager;
import de.tum.in.tumcampus.models.managers.TransportManager;

/**
 * Activity to show transport stations and departures
 */
public class TransportationActivity extends ActivityForSearchingInBackground<Cursor> implements OnItemClickListener {

    private ListView listViewResults;
    private SimpleCursorAdapter adapterStations;
    private RecentsManager recentsManager;

    public TransportationActivity() {
        super(R.layout.activity_transportation, MVVStationSuggestionProvider.AUTHORITY, 3);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get all stations from db
        recentsManager = new RecentsManager(this, RecentsManager.STATIONS);

        listViewResults = (ListView) findViewById(R.id.activity_transport_listview_result);
        listViewResults.setOnItemClickListener(this);

        // Initialize stations adapter
        Cursor stationCursor = recentsManager.getAllFromDb();
        adapterStations = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, stationCursor,
                stationCursor.getColumnNames(), new int[]{android.R.id.text1});

        if(mQuery==null) {
            if(adapterStations.getCount()==0) {
                openSearch();
            } else {
                listViewResults.setAdapter(adapterStations);
                listViewResults.requestFocus();
            }
        }
    }

    /**
     * Click on station in list
     * */
    @Override
    public void onItemClick(final AdapterView<?> av, View v, int position, long id) {
        Cursor departureCursor = (Cursor) av.getAdapter().getItem(position);
        showStation(departureCursor.getString(departureCursor.getColumnIndex(Const.NAME_COLUMN)));
    }

    /**
     * Opens {@link TransportationDetailsActivity} with departure times for the specified station
     * @param station Station
     */
    void showStation(String station) {
        Intent intent = new Intent(this, TransportationDetailsActivity.class);
        intent.putExtra(TransportationDetailsActivity.EXTRA_STATION, station);
        startActivity(intent);
    }

    /**
     * Shows all recently used stations
     * @return Cursor holding the recents information (name, _id)
     */
    @Override
    public Cursor onSearchInBackground() {
        return recentsManager.getAllFromDb();
    }

    /**
     * Searches the Webservice for stations
     *
     * @param query the text entered by the user
     * @return Cursor holding the stations (name, _id)
     */
    @Override
    public Cursor onSearchInBackground(String query) {
        // TODO: Workaround, because MVV does not find a station with the full name as a text input
        String inputTextToCheck = query;
        if (inputTextToCheck.length() > 2) {
            inputTextToCheck = inputTextToCheck.substring(0, inputTextToCheck.length() - 1);
        }
        final String inputText = inputTextToCheck;

        // Get Information
        Cursor stationCursor = null;
        try {
            stationCursor = TransportManager.getStationsFromExternal(inputText);
        } catch (NoSuchElementException e) {
        } catch (TimeoutException e) {
            showNoInternetLayout();
        } catch (Exception e) {
            showError(R.string.exception_unknown);
        }

        // Drop results if canceled
        if (asyncTask.isCancelled()) {
            return null;
        }

        return stationCursor;
    }

    /**
     * Shows the stations
     * @param stationCursor Cursor with stations (name, _id)
     */
    @Override
    protected void onSearchFinished(Cursor stationCursor) {
        // If there is exactly one station open results directly
        if(stationCursor!=null && stationCursor.getCount()==1 && mQuery!=null) {
            stationCursor.moveToFirst();
            showStation(stationCursor.getString(0));
            return;
        } else if(stationCursor==null) {
            listViewResults.setAdapter(new NoResultsAdapter(this));
            listViewResults.requestFocus();
            return;
        }

        adapterStations.changeCursor(stationCursor);
        listViewResults.setAdapter(adapterStations);
        listViewResults.requestFocus();
    }
}