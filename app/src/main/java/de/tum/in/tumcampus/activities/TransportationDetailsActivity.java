package de.tum.in.tumcampus.activities;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.LinearLayout;

import java.util.NoSuchElementException;
import java.util.concurrent.TimeoutException;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.generic.ActivityForLoadingInBackground;
import de.tum.in.tumcampus.auxiliary.DepartureView;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.managers.TransportManager;

/**
 * Activity to show transport stations and departures
 */
public class TransportationDetailsActivity extends ActivityForLoadingInBackground<String,Cursor> {
    public static final String EXTRA_STATION = "station";

    private LinearLayout mViewResults;
    private TransportManager mTransportationManager;

    public TransportationDetailsActivity() {
        super(R.layout.activity_transportation_detail);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get all stations from db
        mTransportationManager = new TransportManager(this);

        mViewResults = (LinearLayout) this.findViewById(R.id.activity_transport_result);

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
        mTransportationManager.replaceIntoDb(location);

        // get departures from website
        Cursor departureCursor = null;
        try {
            departureCursor = mTransportationManager.getDeparturesFromExternal(location);
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
        if(result.moveToFirst()) {
            do {
                DepartureView view = new DepartureView(this, true);
                view.setSymbol(result.getString(0));
                view.setLine(result.getString(1));
                view.setTime(System.currentTimeMillis()+result.getLong(2)*60000);
                mViewResults.addView(view);
            } while(result.moveToNext());
        }
    }
}