package de.tum.in.tumcampus.activities;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.NoSuchElementException;
import java.util.concurrent.TimeoutException;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.generic.ActivityForSearching;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.ImplicitCounter;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.managers.TransportManager;

/**
 * Activity to show transport stations and departures
 */
public class TransportationActivity extends ActivityForSearching implements OnItemClickListener {

    private TextView infoTextView;
    private ListView listViewResults;

    //private ListView listViewSuggestionsAndSaved = null;

    private Thread runningSearch = null;
    private SimpleCursorAdapter adapterStations;
    private SimpleCursorAdapter adapterResults;

    public TransportationActivity() {
        super(R.layout.activity_transportation);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPrefs.getBoolean("implicitly_id", true)) {
            ImplicitCounter.Counter("mvv_id", this.getApplicationContext());
        }

        // get all stations from db
        TransportManager transportationManager = new TransportManager(this);
        Cursor stationCursor = transportationManager.getAllFromDb();

        listViewResults = (ListView) this.findViewById(R.id.activity_transport_listview_result);
        this.infoTextView = (TextView) this.findViewById(R.id.activity_transport_textview_info);

        // Initialize stations adapter
        adapterStations = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, stationCursor,
                stationCursor.getColumnNames(), new int[]{android.R.id.text1});

        // initialize empty departure adapter, disable on click in list
        MatrixCursor departureCursor = new MatrixCursor(new String[]{"symbol", "name", "time", "_id"});
        adapterResults = new SimpleCursorAdapter(this, R.layout.card_departure_line, departureCursor,
                departureCursor.getColumnNames(), new int[]{R.id.line_symbol, R.id.line_name, R.id.line_time}) {
            @Override
            public boolean isEnabled(int position) {
                return false;
            }
        };
        adapterResults.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int i) {
                if (view.getId() == R.id.line_symbol) {
                    TransportManager.setSymbol(TransportationActivity.this, (ImageView) view, cursor.getString(0));
                    return true;
                }
                return false;
            }
        });
        listViewResults.setAdapter(adapterStations);
        listViewResults.setOnItemClickListener(this);
        listViewResults.requestFocus();
    }
/*
    public void onClick(View view) {

        this.infoTextView.setVisibility(View.GONE);

        int viewId = view.getId();
        switch (viewId) {
            case R.id.activity_transport_domore:
                Cursor stationCursor = this.transportaionManager.getAllFromDb();

                if (!this.transportaionManager.empty()) {
                    SimpleCursorAdapter adapter = (SimpleCursorAdapter) this.listViewSuggestionsAndSaved.getAdapter();
                    adapter.changeCursor(stationCursor);
                } else {
                    this.infoTextView.setText(getString(R.string.no_stored_search_requests));
                    this.infoTextView.setVisibility(View.VISIBLE);
                }
                this.listViewSuggestionsAndSaved.setVisibility(View.VISIBLE);
                this.listViewResults.setVisibility(View.GONE);
                this.errorLayout.setVisibility(View.GONE);
                Utils.hideKeyboard(this, this.searchTextField);
                break;
        }
    }*/

    /**
     * Click on station in list
     * */
    @Override
    public void onItemClick(final AdapterView<?> av, View v, int position, long id) {

        Cursor departureCursor = (Cursor) av.getAdapter().getItem(position);
        final String location = departureCursor.getString(departureCursor.getColumnIndex(Const.NAME_COLUMN));

        this.listViewResults.setEnabled(true);
        //this.searchTextField.setText(location);

        showStation(location);
    }

    public void showStation(final String location) {

        // save clicked station into db and refresh station list
        // (could be clicked on search result list)
        TransportManager tm = new TransportManager(this);
        tm.replaceIntoDb(location);

        this.progressLayout.setVisibility(View.VISIBLE);
        this.infoTextView.setVisibility(View.GONE);

        if (!Utils.isConnected(this)) {
            this.progressLayout.setVisibility(View.GONE);
            this.errorLayout.setVisibility(View.VISIBLE);
            Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
            adapterResults.changeCursor(tm.getAllFromDb());
            listViewResults.setAdapter(adapterResults);
            return;
        }

        new Thread(new Runnable() {
            int message;

            @Override
            public void run() {
                // get departures from website
                TransportManager tm = new TransportManager(TransportationActivity.this);
                Cursor departureCursor = null;
                try {
                    departureCursor = tm.getDeparturesFromExternal(location);
                } catch (NoSuchElementException e) {
                    this.message = R.string.no_departures_found;
                } catch (TimeoutException e) {
                    this.message = R.string.exception_timeout;
                } catch (Exception e) {
                    this.message = R.string.exception_unknown;
                }

                // show departures in list
                TransportationActivity.this.runOnUiThread(new ResultUpdater(departureCursor, adapterResults, message));
            }
        }).start();
    }

    @Override
    public void onBackPressed() {
        if (runningSearch != null) {
            runningSearch.interrupt();
            progressLayout.setVisibility(View.GONE);
            runningSearch = null;
        } else {
            finish();
        }
    }

    /**
     * Searchs the Webservice for stations
     *
     * @param query the text entered by the user
     */

    @Override
    public void performSearchAlgorithm(String query) {
        // TODO: Workaround, because MVV does not find a station with the full name as a text input
        String inputTextToCheck = query;
        if (query.length() > 2) {
            inputTextToCheck = query.substring(0, query.length() - 1);
        }
        final String inputText = inputTextToCheck;
        infoTextView.setVisibility(View.GONE);

        runningSearch = new Thread(new Runnable() {

            @Override
            public void run() {
                // Remember my Thread
                Thread me = TransportationActivity.this.runningSearch;

                // Get Information
                TransportManager tm = new TransportManager(TransportationActivity.this);
                Cursor stationCursor = null;
                int message = 0;
                try {
                    stationCursor = tm.getStationsFromExternal(inputText);
                } catch (NoSuchElementException e) {
                    message = R.string.no_station_found;
                } catch (TimeoutException e) {
                    message = R.string.exception_timeout;
                } catch (Exception e) {
                    message = R.string.exception_unknown;
                }

                // Drop results if canceled
                if (TransportationActivity.this.runningSearch == null || TransportationActivity.this.runningSearch != me) {
                    return;
                }

                // If there is exactly one station open results directly
                if(stationCursor!=null && stationCursor.getCount()==1) {
                    stationCursor.moveToFirst();
                    showStation(stationCursor.getString(0));
                    return;
                }

                // show stations from search result in station list, show error message if necessary
                TransportationActivity.this.runOnUiThread(new ResultUpdater(stationCursor, adapterStations, message));
            }
        });
        runningSearch.start();
    }

    /**
     * Brings the results of the query to ui
    * */
    private class ResultUpdater implements Runnable {

        private final SimpleCursorAdapter mAdapter;
        private final Cursor mCursor;
        private final int mMessage;

        private ResultUpdater(Cursor cursor, SimpleCursorAdapter adapter, int message) {
            mCursor = cursor;
            mAdapter = adapter;
            mMessage = message;
        }

        @Override
        public void run() {
            mAdapter.changeCursor(mCursor);
            listViewResults.setAdapter(mAdapter);

            TransportationActivity.this.progressLayout.setVisibility(View.GONE);
            TransportationActivity.this.errorLayout.setVisibility(View.GONE);

            if (mMessage!= 0) {
                TransportationActivity.this.infoTextView.setText(mMessage);
                TransportationActivity.this.infoTextView.setVisibility(View.VISIBLE);
            }
            runningSearch = null;
        }
    }
}