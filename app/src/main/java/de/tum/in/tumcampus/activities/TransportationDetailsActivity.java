package de.tum.in.tumcampus.activities;

import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.NoSuchElementException;
import java.util.concurrent.TimeoutException;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.generic.ActivityForDownloadingExternal;
import de.tum.in.tumcampus.activities.generic.ActivityForSearching;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.ImplicitCounter;
import de.tum.in.tumcampus.auxiliary.MVVStationSuggestionProvider;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.managers.TransportManager;

/**
 * Activity to show transport stations and departures
 */
public class TransportationDetailsActivity extends ActionBarActivity {
    public static final String EXTRA_STATION = "station";

    private TextView infoTextView;
    private ListView listViewResults;
    private Thread runningSearch = null;
    private SimpleCursorAdapter adapterResults;
    private TransportManager transportationManager;
    protected RelativeLayout errorLayout;
    protected RelativeLayout progressLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_transportation);
        progressLayout = (RelativeLayout) findViewById(R.id.progress_layout);
        errorLayout = (RelativeLayout) findViewById(R.id.error_layout);

        // get all stations from db
        transportationManager = new TransportManager(this);

        infoTextView = (TextView) this.findViewById(R.id.activity_transport_textview_info);
        listViewResults = (ListView) this.findViewById(R.id.activity_transport_listview_result);

        // initialize empty departure adapter, disable on click in list
        MatrixCursor departureCursor = new MatrixCursor(new String[]{"symbol", "name", "time", "_id"});
        adapterResults = new SimpleCursorAdapter(this, R.layout.activity_transportation_departure_listview, departureCursor,
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
                    TransportManager.setSymbol(TransportationDetailsActivity.this, (TextView) view, cursor.getString(0));
                    return true;
                }
                return false;
            }
        });
        listViewResults.setAdapter(adapterResults);

        Intent intent = getIntent();
        if(intent==null) {
            finish();
            return;
        }
        String location = intent.getStringExtra(EXTRA_STATION);
        setTitle(location);
        showStation(location);
    }

    public void showStation(final String location) {
        // save clicked station into db and refresh station list
        // (could be clicked on search result list)
        transportationManager.replaceIntoDb(location);

        this.progressLayout.setVisibility(View.VISIBLE);
        this.infoTextView.setVisibility(View.GONE);

        if (!Utils.isConnected(this)) {
            this.progressLayout.setVisibility(View.GONE);
            this.errorLayout.setVisibility(View.VISIBLE);
            Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
            adapterResults.changeCursor(transportationManager.getAllFromDb());
            listViewResults.setAdapter(adapterResults);
            return;
        }

        new Thread(new Runnable() {
            int message;

            @Override
            public void run() {
                // get departures from website
                Cursor departureCursor = null;
                try {
                    departureCursor = transportationManager.getDeparturesFromExternal(location);
                } catch (NoSuchElementException e) {
                    this.message = R.string.no_departures_found;
                } catch (TimeoutException e) {
                    this.message = R.string.exception_timeout;
                } catch (Exception e) {
                    this.message = R.string.exception_unknown;
                }

                // show departures in list
                TransportationDetailsActivity.this.runOnUiThread(new ResultUpdater(departureCursor, message));
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Brings the results of the query to ui
    * */
    private class ResultUpdater implements Runnable {

        private final Cursor mCursor;
        private final int mMessage;

        private ResultUpdater(Cursor cursor, int message) {
            mCursor = cursor;
            mMessage = message;
        }

        @Override
        public void run() {
            adapterResults.changeCursor(mCursor);

            TransportationDetailsActivity.this.progressLayout.setVisibility(View.GONE);
            TransportationDetailsActivity.this.errorLayout.setVisibility(View.GONE);

            if (mMessage!= 0) {
                TransportationDetailsActivity.this.infoTextView.setText(mMessage);
                TransportationDetailsActivity.this.infoTextView.setVisibility(View.VISIBLE);
            }
            TransportationDetailsActivity.this.listViewResults.requestFocus();
            runningSearch = null;
        }
    }
}