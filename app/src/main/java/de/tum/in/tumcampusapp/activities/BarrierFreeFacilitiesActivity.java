package de.tum.in.tumcampusapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.google.common.base.Optional;

import java.io.IOException;
import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.ActivityForLoadingInBackground;
import de.tum.in.tumcampusapp.adapters.RoomFinderListAdapter;
import de.tum.in.tumcampusapp.api.TUMCabeClient;
import de.tum.in.tumcampusapp.auxiliary.NetUtils;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.managers.LocationManager;
import de.tum.in.tumcampusapp.managers.RecentsManager;
import de.tum.in.tumcampusapp.models.tumcabe.RoomFinderRoom;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class BarrierFreeFacilitiesActivity extends ActivityForLoadingInBackground<Void, Optional<List<RoomFinderRoom>>>
        implements AdapterView.OnItemSelectedListener, AdapterView.OnItemClickListener {
    private int selectedFacilityPage = -1;
    private StickyListHeadersListView stickyList;
    private List<RoomFinderRoom> facilities;

    private RecentsManager recentsManager;
    private LocationManager locationManager;

    public BarrierFreeFacilitiesActivity() {
        super(R.layout.activity_barrier_free_facilities);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set spinner
        Spinner spinner = findViewById(R.id.spinnerToolbar);
        spinner.setOnItemSelectedListener(this);

        // manager
        recentsManager = new RecentsManager(this, RecentsManager.ROOMS);
        locationManager = new LocationManager(this);

        stickyList = findViewById(R.id.activity_barrier_free_facilities_list_view);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        selectedFacilityPage = position;
        startLoading();
    }

    @Override
    protected Optional<List<RoomFinderRoom>> onLoadInBackground(Void... arg) {
        showLoadingStart();
        List<RoomFinderRoom> result;
        TUMCabeClient cabeClient = TUMCabeClient.getInstance(this);

        try {
            switch (selectedFacilityPage) {
                case 0:
                    // Nearby staff - need to fetch building id first
                    Optional<String> buildingId = locationManager.getBuildingIDFromCurrentLocation();
                    if (buildingId.isPresent()) {
                        result = cabeClient.getListOfNearbyFacilities(buildingId.get());
                    } else {
                        return Optional.absent();
                    }
                    break;

                case 1:
                    result = cabeClient.getListOfToilets();
                    break;

                case 2:
                    result = cabeClient.getListOfElevators();
                    break;
                default:
                    return Optional.absent();
            }
        } catch (IOException e) {
            Utils.log(e);
            return Optional.absent();
        }

        if (result == null) {
            return Optional.absent();
        }

        return Optional.of(result);
    }

    @Override
    protected void onLoadFinished(Optional<List<RoomFinderRoom>> result) {
        showLoadingEnded();
        if (!result.isPresent()) {
            if (NetUtils.isConnected(this)) {
                showErrorLayout();
            } else {
                showNoInternetLayout();
            }
            return;
        }

        facilities = result.get();
        StickyListHeadersAdapter adapter = new RoomFinderListAdapter(this, facilities);
        stickyList.setAdapter(adapter);
        stickyList.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        RoomFinderRoom facility = facilities.get(position);

        recentsManager.replaceIntoDb(facility.toString());

        // Start detail activity
        Intent intent = new Intent(this, RoomFinderDetailsActivity.class);
        intent.putExtra(RoomFinderDetailsActivity.EXTRA_ROOM_INFO, facility);
        startActivity(intent);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Nothing seleced
    }
}
