package de.tum.in.tumcampusapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.ActivityForLoadingInBackground;
import de.tum.in.tumcampusapp.auxiliary.DepartureView;
import de.tum.in.tumcampusapp.auxiliary.NetUtils;
import de.tum.in.tumcampusapp.models.managers.RecentsManager;
import de.tum.in.tumcampusapp.models.managers.TransportManager;

/**
 * Activity to show transport departures for a specified station
 * <p/>
 * NEEDS: EXTRA_STATION set in incoming bundle (station name)
 */
public class TransportationDetailsActivity extends ActivityForLoadingInBackground<String, List<TransportManager.Departure>> {
    public static final String EXTRA_STATION = "station";
    public static final String EXTRA_STATION_ID = "stationID";

    private LinearLayout mViewResults;
    private RecentsManager recentsManager;

    public TransportationDetailsActivity() {
        super(R.layout.activity_transportation_detail);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get all stations from db
        recentsManager = new RecentsManager(this, RecentsManager.STATIONS);
        mViewResults = (LinearLayout) this.findViewById(R.id.activity_transport_result);

        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }
        String location = intent.getStringExtra(EXTRA_STATION);
        setTitle(location);
        String locationID = intent.getStringExtra(EXTRA_STATION_ID);

        startLoading(location, locationID);
    }

    /**
     * Load departure times
     *
     * @param arg Station name
     * @return List of departures
     */
    @Override
    protected List<TransportManager.Departure> onLoadInBackground(String... arg) {
        final String location = arg[0];

        // save clicked station into db
        recentsManager.replaceIntoDb(location);

        // Check for internet connectivity
        if (!NetUtils.isConnected(this)) {
            showNoInternetLayout();
            return null;
        }

        // get departures from website
        final String locationID = arg[1];
        List<TransportManager.Departure> departureCursor = TransportManager.getDeparturesFromExternal(this, locationID);
        if (departureCursor == null) {
            showError(R.string.no_departures_found);
        }

        return departureCursor;
    }

    /**
     * Adds a new {@link DepartureView} for each departure entry
     *
     * @param result List of departures
     */
    @Override
    protected void onLoadFinished(List<TransportManager.Departure> result) {
        showLoadingEnded();
        if (result == null) {
            return;
        }
        mViewResults.removeAllViews();
        for (TransportManager.Departure d : result) {
            DepartureView view = new DepartureView(this, true);
            view.setSymbol(d.symbol);
            view.setLine(d.servingLine);
            view.setTime(d.countDown);
            mViewResults.addView(view);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (int i = 0; i < mViewResults.getChildCount(); i++) {
            View view = mViewResults.getChildAt(i);
            if (!(view instanceof DepartureView)) {
                continue;
            }
            ((DepartureView) view).removeAllCallbacksAndMessages();
        }
    }
}