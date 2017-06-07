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

/**
 * Activity to fetch and display the curricula of different study programs.
 */
public class FacilityCategoriesActivity extends ActivityForLoadingInBackground<Void, Optional<JSONArray>> implements OnItemClickListener {
    public static final String CATEGORY_ID = "category_id";

    public static final String FACILITY = "https://tumcabe.in.tum.de/Api/curricula";

    private Map<String, String> options;
    private ArrayAdapter<String> arrayAdapter;
    private NetUtils net;

    public FacilityCategoriesActivity() {
        super(R.layout.activity_facility_categories);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        net = new NetUtils(this);
        // Sets the adapter
        ListView list = (ListView) this.findViewById(R.id.activity_facility_categories_list_view);
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        list.setAdapter(arrayAdapter);
        list.setOnItemClickListener(this);

        // Fetch all curricula from webservice via parent async class
        this.startLoading();
    }

    @Override
    protected Optional<JSONArray> onLoadInBackground(Void... arg) {

        //TODO:Link to actual backend
        try {
            return Optional.of(new JSONArray("[{id:1,name:Library,url:'http://google.com'},{id:2,name:Cafeteria,url:'http://google.com'}]"));
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
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
                options.put(item.getString("name"), item.getString("id"));
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
        String categoryName = ((TextView) view).getText().toString();

        // Puts URL and name into an intent and starts the detail view
        Intent intent = new Intent(this, FacilityActivity.class);
        intent.putExtra(CATEGORY_ID, options.get(categoryName));
        this.startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_facility, menu);
        MenuItem tagFacility = menu.findItem(R.id.action_tag_facility);
        tagFacility.setVisible(true);
        return true;
    }
}
