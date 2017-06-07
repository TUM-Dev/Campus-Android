package de.tum.in.tumcampusapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
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
import de.tum.in.tumcampusapp.activities.generic.ActivityForLoadingInBackground;
import de.tum.in.tumcampusapp.auxiliary.NetUtils;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.managers.CacheManager;

import static de.tum.in.tumcampusapp.activities.CurriculaActivity.CURRICULA_URL;

/**
 * Activity to fetch and display the curricula of different study programs.
 */
public class FacilityActivity extends ActivityForLoadingInBackground<String, Optional<JSONArray>> implements OnItemClickListener {
    public static final String NAME = "name";
    public static final String URL = "url";

    public static final String FACILITY = "https://tumcabe.in.tum.de/Api/curricula";

    private Map<String, String> options;
    private ArrayAdapter<String> arrayAdapter;
    private NetUtils net;

    private static final String MOCK_FACILITIES="[{facility_id:1,name:'Garching Library',position:1234,category_id:1}," +
                                                "{facility_id:2,name:'Main campus Library',position:1234,category_id:1}," +
                                                "{facility_id:3,name:'Stucafe Informatics',position:1234,category_id:2}," +
                                                "{facility_id:4,name:'Stucafe Mechanical',position:1234,category_id:2}]";

    public FacilityActivity() {
        super(R.layout.activity_facility);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        net = new NetUtils(this);
        // Sets the adapter
        ListView list = (ListView) this.findViewById(R.id.activity_facility_list_view);
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        list.setAdapter(arrayAdapter);
        list.setOnItemClickListener(this);

        String category_id = getIntent().getExtras().getString(FacilityCategoriesActivity.CATEGORY_ID);

        // Fetch all curricula from webservice via parent async class
        this.startLoading(category_id);
    }

    @Override
    protected Optional<JSONArray> onLoadInBackground(String... params) {
            return getMockFacilities(params[0]);
//        return net.downloadJsonArray(CURRICULA_URL, CacheManager.VALIDITY_ONE_MONTH, false);
    }


    private Optional<JSONArray> getMockFacilities(String id){
        JSONArray result=new JSONArray();
        try {
            JSONArray mockFacilitiesJson=new JSONArray(MOCK_FACILITIES);
            for (int i = 0; i < mockFacilitiesJson.length(); i++) {
                JSONObject item = mockFacilitiesJson.getJSONObject(i);
                if(item.getString("category_id").equals(id)){
                    result.put(item);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return Optional.of(result);
    }

    @Override
    protected void onLoadFinished(Optional<JSONArray> jsonData) {
        if (!jsonData.isPresent()) {
            if (NetUtils.isConnected(this)) {
                showErrorLayout();
            } else {
                showNoInternetLayout();
            }
            return;
        }
        JSONArray arr = jsonData.get();
        try {
            options = new HashMap<>();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject item = arr.getJSONObject(i);

                arrayAdapter.add(item.getString("name"));
                options.put(item.getString("name"), item.getString("position"));
            }
        } catch (JSONException e) {
            Utils.log(e);
        }
        showLoadingEnded();
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

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_facility, menu);
        MenuItem tagFacility = menu.findItem(R.id.action_tag_facility);
        tagFacility.setVisible(true);
        return true;
    }
}
