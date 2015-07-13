package de.tum.in.tumcampus.activities;

import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.generic.ActivityForSearchingInBackground;
import de.tum.in.tumcampus.adapters.NoResultsAdapter;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.MVVStationSuggestionProvider;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.managers.RecentsManager;
import de.tum.in.tumcampus.models.managers.TransportManager;

/**
 * Activity to show transport stations and departures
 */
public class TransportationActivity extends ActivityForSearchingInBackground<Cursor> implements OnItemClickListener {

    private ListView listViewResults;
    private SimpleCursorAdapter adapterStations;
    private RecentsManager recentsManager;
    private boolean returnResult, setResult;

    public TransportationActivity() {
        super(R.layout.activity_transportation, MVVStationSuggestionProvider.AUTHORITY, 3);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        returnResult = getCallingActivity() != null;
        setResult = false;

        // get all stations from db
        recentsManager = new RecentsManager(this, RecentsManager.STATIONS);

        listViewResults = (ListView) findViewById(R.id.activity_transport_listview_result);
        listViewResults.setOnItemClickListener(this);

        // Initialize stations adapter
        Cursor stationCursor = recentsManager.getAllFromDb();
        adapterStations = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, stationCursor,
                stationCursor.getColumnNames(), new int[]{android.R.id.text1});

        if (adapterStations.getCount() == 0) {
            openSearch();
        } else {
            listViewResults.setAdapter(adapterStations);
            listViewResults.requestFocus();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (returnResult && !setResult) {
            // user cancelled station selection
            setResult(RESULT_CANCELED);
        }
    }

    /**
     * Click on station in list
     * */
    @Override
    public void onItemClick(final AdapterView<?> av, View v, int position, long id) {
        Cursor departureCursor = (Cursor) av.getAdapter().getItem(position);
        String station = departureCursor.getString(departureCursor.getColumnIndex(Const.NAME_COLUMN));

        // check, if activity result is needed
        if (!returnResult) {
            // called by navigation drawer => no result
            // v.animate().setDuration(5000).rotationBy(360).start();
            showStation(station);
        } else {
            // called by preferences screen => return result
            Intent result = new Intent();
            result.putExtra("station", station);
            setResult(RESULT_OK, result);

            setResult = true;
            finish();
        }
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
            stationCursor = TransportManager.getStationsFromExternal(this, inputText);
        } catch (TimeoutException | IOException e) {
            showNoInternetLayout();
        } catch (Exception e) {
            Utils.log(e);
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
        if(stationCursor==null)
            return;

        showLoadingEnded();

        // mQuery is not null if it was a real search
        // If there is exactly one station, open results directly
        if(stationCursor.getCount()==1 && mQuery!=null) {
            if (!returnResult) {
                stationCursor.moveToFirst();
                showStation(stationCursor.getString(0));
                return;
            } else {
                // called by preferences screen => return result
                Intent result = new Intent();
                result.putExtra("station", stationCursor.getString(0));
                setResult(RESULT_OK, result);

                setResult = true;
                finish();
            }
        } else if(stationCursor.getCount()==0) {
            // When stationCursor is a MatrixCursor the result comes from querying a station name
            if(stationCursor instanceof MatrixCursor) {
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

        adapterStations.changeCursor(stationCursor);
        listViewResults.setAdapter(adapterStations);
        listViewResults.requestFocus();
    }
}