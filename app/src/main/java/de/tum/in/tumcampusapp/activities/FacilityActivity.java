package de.tum.in.tumcampusapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.common.base.Optional;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import de.tum.in.tumcampusapp.R;

import de.tum.in.tumcampusapp.activities.generic.ActivityForSearching;
import de.tum.in.tumcampusapp.auxiliary.FacilityLocatorSuggestionProvider;
import de.tum.in.tumcampusapp.auxiliary.NetUtils;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.tumonline.TUMFacilityLocatorRequest;
import de.tum.in.tumcampusapp.tumonline.TUMFacilityLocatorRequestFetchListener;

/**
 * Activity to fetch and display the curricula of different study programs.
 */
public class FacilityActivity extends ActivityForSearching implements OnItemClickListener,TUMFacilityLocatorRequestFetchListener {

    private TUMFacilityLocatorRequest facilityLocatorRequest;

    public static final String LONGITUDE = "longitude";
    public static final String LATITUDE = "latitude";
    public static final String FACILITY_NAME = "name";
    public static final String FACILITY_ID = "facility_id";
    public static final String FACILITY_CATEGORY_ID = "category_id";



    private Map<String, String> options;
    private ArrayAdapter<String> arrayAdapter;
    private NetUtils net;


    public FacilityActivity() {
        super(R.layout.activity_facility, FacilityLocatorSuggestionProvider.AUTHORITY,3);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        facilityLocatorRequest = new TUMFacilityLocatorRequest(this);

        net = new NetUtils(this);
        // Sets the adapter
        ListView list = (ListView) this.findViewById(R.id.activity_facility_list_view);
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        list.setAdapter(arrayAdapter);
        list.setOnItemClickListener(this);

        String categoryId = getIntent().getExtras().getString(FacilityCategoriesActivity.FACILITY_CATEGORY_ID);
        String facilitySearchQuery = getIntent().getExtras().getString(FacilityCategoriesActivity.FACILITY_SEARCH_QUERY);

        if(categoryId!=null){
            setTitle("Category : "+getIntent().getExtras().getString(FacilityCategoriesActivity.FACILITY_CATEGORY_NAME));
            facilityLocatorRequest.fetchSearchInteractiveFacilities(this, this, categoryId, true);
        }else if(facilitySearchQuery!=null){
            setTitle("Search : "+facilitySearchQuery);
            this.requestSearch(facilitySearchQuery);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    /**
     * Handle click on curricula item
     *
     * @param parent Containing listView
     * @param view   Item view
     * @param pos    Index of item
     * @param id     Id of item
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
        String facilityName = ((TextView) view).getText().toString();

        // Puts URL and name into an intent and starts the detail view
        Intent intent = new Intent(this, FacilityDisplayActivity.class);
        String[] tokens=options.get(facilityName).split("-");
        double longitude= Double.parseDouble(tokens[0]);
        double latitude= Double.parseDouble(tokens[1]);
        intent.putExtra(FACILITY_NAME, facilityName);
        intent.putExtra(LONGITUDE, longitude);
        intent.putExtra(LATITUDE, latitude);
        this.startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStartSearch() {

    }

    @Override
    protected void onStartSearch(String query) {
        facilityLocatorRequest.fetchSearchInteractiveFacilities(this, this, query, false);
    }

    @Override
    public void onFetch(Optional<JSONArray> result) {
        if (result.isPresent()) {
            fillListView(result);
            showLoadingEnded();
        }
    }

    @Override
    public void onFetchError(String errorReason) {
        facilityLocatorRequest.cancelRequest(true);
        showError(errorReason);
    }

    @Override
    public void onNoInternetError() {
        showNoInternetLayout();
    }

    private void fillListView(Optional<JSONArray> jsonData) {
        if (!jsonData.isPresent()) {
            if (NetUtils.isConnected(this)) {
                showErrorLayout();
            } else {
                showNoInternetLayout();
            }
            return;
        }
        JSONArray arr = jsonData.get();
        arrayAdapter.clear();
        try {
            options = new HashMap<>();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject item = arr.getJSONObject(i);
                arrayAdapter.add(item.getString("name"));
                options.put(item.getString("name"), item.getDouble("longitude")+"-"+item.getDouble("latitude"));
            }
        } catch (JSONException e) {
            Utils.log(e);
        }
        showLoadingEnded();
    }
}
