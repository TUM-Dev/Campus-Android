package de.tum.in.tumcampus.activities;

import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.util.NoSuchElementException;
import java.util.concurrent.TimeoutException;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.generic.ActivityForLoadingInBackground;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.managers.TransportManager;

/**
 * Activity to show transport stations and departures
 */
public class TransportationDetailsActivity extends ActivityForLoadingInBackground<String,Cursor> {
    public static final String EXTRA_STATION = "station";

    private ListView listViewResults;
    private SimpleCursorAdapter adapterResults;
    private TransportManager transportationManager;

    public TransportationDetailsActivity() {
        super(R.layout.activity_transportation);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get all stations from db
        transportationManager = new TransportManager(this);

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


        if (!Utils.isConnected(this)) {
            showError(R.string.no_internet_connection);
        } else {
            startLoading(location);
        }
    }

    @Override
    protected Cursor onLoadInBackground(String[] arg) {
        final String location = arg[0];
        // save clicked station into db and refresh station list
        // (could be clicked on search result list)
        transportationManager.replaceIntoDb(location);

        // get departures from website
        Cursor departureCursor = null;
        try {
            departureCursor = transportationManager.getDeparturesFromExternal(location);
        } catch (NoSuchElementException e) {
            showError(R.string.no_departures_found);
        } catch (TimeoutException e) {
            showError(R.string.exception_timeout);
        } catch (Exception e) {
            showError(R.string.exception_unknown);
        }

        return departureCursor;
    }

    @Override
    protected void onLoadFinished(Cursor result) {
        adapterResults.changeCursor(result);
        listViewResults.requestFocus();
    }
}