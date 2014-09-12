package de.tum.in.tumcampus.activities;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.util.NoSuchElementException;
import java.util.concurrent.TimeoutException;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.generic.ActivityForSearchingInBackground;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.MVVStationSuggestionProvider;
import de.tum.in.tumcampus.models.managers.TransportManager;

/**
 * Activity to show transport stations and departures
 */
public class TransportationActivity extends ActivityForSearchingInBackground<Cursor> implements OnItemClickListener {

    private ListView listViewResults;
    private SimpleCursorAdapter adapterStations;
    private TransportManager transportationManager;

    public TransportationActivity() {
        super(R.layout.activity_transportation, MVVStationSuggestionProvider.AUTHORITY, 3);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get all stations from db
        transportationManager = new TransportManager(this);

        listViewResults = (ListView) this.findViewById(R.id.activity_transport_listview_result);
        listViewResults.setOnItemClickListener(this);

        // Initialize stations adapter
        Cursor stationCursor = transportationManager.getAllFromDb();
        adapterStations = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, stationCursor,
                stationCursor.getColumnNames(), new int[]{android.R.id.text1});

        onNewIntent(getIntent());

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
        final String location = departureCursor.getString(departureCursor.getColumnIndex(Const.NAME_COLUMN));
        showStation(location);
    }

    public void showStation(String station) {
        Intent intent = new Intent(this, TransportationDetailsActivity.class);
        intent.putExtra(TransportationDetailsActivity.EXTRA_STATION, station);
        startActivity(intent);
    }

    @Override
    public Cursor onSearchInBackground() {
        return transportationManager.getAllFromDb();
    }

    /**
     * Searchs the Webservice for stations
     *
     * @param query the text entered by the user
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
            stationCursor = transportationManager.getStationsFromExternal(inputText);
        } catch (NoSuchElementException e) {
            showError(R.string.no_station_found);
        } catch (TimeoutException e) {
            showError(R.string.exception_timeout);
        } catch (Exception e) {
            showError(R.string.exception_unknown);
        }

        // Drop results if canceled
        if (asyncTask.isCancelled()) {
            return null;
        }

        return stationCursor;
    }

    @Override
    protected void onSearchFinished(Cursor stationCursor) {
        // If there is exactly one station open results directly
        if(stationCursor!=null && stationCursor.getCount()==1 && mQuery!=null) {
            stationCursor.moveToFirst();
            final String location = stationCursor.getString(0);
            showStation(location);
            return;
        }

        adapterStations.changeCursor(stationCursor);
        listViewResults.requestFocus();
    }
}