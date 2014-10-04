package de.tum.in.tumcampus.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeoutException;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.generic.ActivityForLoadingInBackground;
import de.tum.in.tumcampus.auxiliary.DepartureView;
import de.tum.in.tumcampus.auxiliary.NetUtils;
import de.tum.in.tumcampus.models.managers.RecentsManager;
import de.tum.in.tumcampus.models.managers.TransportManager;

/**
 * Activity to show transport departures for a specified station
 *
 * NEEDS: EXTRA_STATION set in incoming bundle (station name)
 */
public class TransportationDetailsActivity extends ActivityForLoadingInBackground<String,List<TransportManager.Departure>> {
    public static final String EXTRA_STATION = "station";

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
        if(intent==null) {
            finish();
            return;
        }
        String location = intent.getStringExtra(EXTRA_STATION);
        setTitle(location);

        startLoading(location);
    }

    /**
     * Load departure times
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
        List<TransportManager.Departure> departureCursor = null;
        try {
            departureCursor = TransportManager.getDeparturesFromExternal(this, location);
        } catch (NoSuchElementException e) {
            showError(R.string.no_departures_found);
        } catch (TimeoutException e) {
            showNoInternetLayout();
        } catch (Exception e) {
            showError(R.string.exception_unknown);
        }

        return departureCursor;
    }

    /**
     * Adds a new {@link DepartureView} for each departure entry
     * @param result List of departures
     */
    @Override
    protected void onLoadFinished(List<TransportManager.Departure> result) {
        showLoadingEnded();
        if(result==null)
            return;
        mViewResults.removeAllViews();
        for(TransportManager.Departure d : result) {
            DepartureView view = new DepartureView(this, true);
            view.setSymbol(d.symbol);
            view.setLine(d.line);
            view.setTime(System.currentTimeMillis()+d.time*60000);
            mViewResults.addView(view);
        }
    }
}