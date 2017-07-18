package de.tum.in.tumcampusapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import de.tum.in.tumcampusapp.R;

import de.tum.in.tumcampusapp.activities.generic.ActivityForLoadingInBackground;
import de.tum.in.tumcampusapp.adapters.RoomFinderListAdapter;
import de.tum.in.tumcampusapp.api.TUMCabeClient;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.managers.LocationManager;
import de.tum.in.tumcampusapp.managers.RecentsManager;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class BarrierFreeFacilitiesActivity extends ActivityForLoadingInBackground<Void, List<Map<String, String>>>
        implements AdapterView.OnItemSelectedListener, AdapterView.OnItemClickListener{
    private int selectedFacilityPage = -1;
    private RoomFinderListAdapter adapter;
    private StickyListHeadersListView stickyList;
    private List<Map<String, String>> facilities;

    private List<Map<String, String>> building2Gps = null;
    private boolean building2GpsFail = false;

    private RecentsManager recentsManager;
    private LocationManager locationManager;

    public BarrierFreeFacilitiesActivity(){
        super(R.layout.activity_barrier_free_facilities);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Activate action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        // set spinner
        Spinner spinner = (Spinner) findViewById(R.id.spinnerToolbar);
        spinner.setOnItemSelectedListener(this);

        // manager
        recentsManager = new RecentsManager(this, RecentsManager.ROOMS);
        locationManager = new LocationManager(this);

        stickyList = (StickyListHeadersListView) findViewById(R.id.activity_barrier_free_facilities_list_view);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        selectedFacilityPage = position;
        startLoading();
    }

    @Override
    protected List<Map<String, String>> onLoadInBackground(Void... arg) {
        if (building2Gps == null && !building2GpsFail) {
            return fetchBuilding2Gps();
        }

        List<Map<String, String>> result = null;
        TUMCabeClient cabeClient = TUMCabeClient.getInstance(this);

        try {
            switch (selectedFacilityPage) {
                case 0:
                    String buildingId = locationManager.getBuildingIDFromCurrentLocation(building2Gps);
                    result = cabeClient.getListOfNearbyFacilities(buildingId);
                    break;
                case 1:
                    result = cabeClient.getListOfToilets();
                    break;
                case 2:
                    result = cabeClient.getListOfElevators();
                    break;
            }
        } catch (IOException e) {
            Utils.log(e);
            return null;
        }

        return result;
    }

    @Override
    protected void onLoadFinished(List<Map<String, String>> result) {
        if (building2Gps == null && !building2GpsFail) {
            if (result == null) {
                building2GpsFail = true;
                showErrorLayout();
                return;
            }
            building2Gps = result;
            startLoading();
            return;
        }

        if (result == null){
            showErrorLayout();
            return;
        }

        facilities = result;
        adapter = new RoomFinderListAdapter(this, facilities);
        stickyList.setAdapter(adapter);
        stickyList.setOnItemClickListener(this);
    }

    public List<Map<String, String>> fetchBuilding2Gps(){
        try {
            return TUMCabeClient.getInstance(this).getBuilding2Gps();
        } catch (IOException e) {
            Utils.log(e);
            building2GpsFail = true;
            showErrorLayout();
            return null;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Map<String, String> facility = facilities.get(position);

        StringBuilder val = new StringBuilder();
        Bundle b = new Bundle();
        for (Map.Entry<String, String> entry : facility.entrySet()) {
            val.append(entry.getKey()).append('=').append(entry.getValue()).append(';');
            b.putString(entry.getKey(), entry.getValue());
        }
        recentsManager.replaceIntoDb(val.toString());

        Intent intent = new Intent(this, RoomFinderDetailsActivity.class);
        intent.putExtra(RoomFinderDetailsActivity.EXTRA_ROOM_INFO, b);
        startActivity(intent);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Nothing seleced
    }
}
