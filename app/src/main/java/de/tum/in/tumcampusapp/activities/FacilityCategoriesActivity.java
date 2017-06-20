package de.tum.in.tumcampusapp.activities;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import de.tum.in.tumcampusapp.tumonline.TUMFacilityLocatorRequest;

/**
 * Activity to fetch and display the curricula of different study programs.
 */
public class FacilityCategoriesActivity extends ActivityForSearching implements OnItemClickListener {
    public static final String FACILITY_CATEGORY_ID = "category_id";
    public static final String FACILITY_CATEGORY_NAME = "category_name";
    public static final String FACILITY_SEARCH_QUERY="facility_search_query";

    private Map<String, String> options;
    private ArrayAdapter<String> arrayAdapter;
    private Button addFacilityButton;

    TUMFacilityLocatorRequest facilityLocatorRequest;


    public FacilityCategoriesActivity() {
        super(R.layout.activity_facility_categories, FacilityLocatorSuggestionProvider.AUTHORITY,3);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.facilityLocatorRequest=new TUMFacilityLocatorRequest(this);

        // Sets the adapter
        ListView list = (ListView) this.findViewById(R.id.activity_facility_categories_list_view);
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        list.setAdapter(arrayAdapter);
        list.setOnItemClickListener(this);
        createCategoriesMenu();

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(SearchManager.QUERY)) {
            requestSearch(intent.getStringExtra(SearchManager.QUERY));
            return;
        }

        addFacilityButton = (Button) findViewById(R.id.add_facility);
        addFacilityButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                openTaggingActivitty();
            }
        });
    }

    private void createCategoriesMenu() {
        try {
            Optional<JSONArray> facilityCategories=facilityLocatorRequest.fetchFacilityCategories();
            if(facilityCategories.isPresent()){
                JSONArray categoriesJSONMenu=facilityCategories.get();
                options = new HashMap<>();
                for (int i = 0; i < categoriesJSONMenu.length(); i++) {
                    JSONObject item = categoriesJSONMenu.getJSONObject(i);
                    arrayAdapter.add(item.getString("name"));
                    options.put(item.getString("name"), item.getString("id"));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
        intent.putExtra(FACILITY_CATEGORY_ID, options.get(categoryName));
        intent.putExtra(FACILITY_CATEGORY_NAME,categoryName);
        this.startActivity(intent);
    }

    @Override
    protected void onStartSearch() {

    }

    @Override
    protected void onStartSearch(String query) {
        Intent intent = new Intent(this, FacilityActivity.class);
        intent.putExtra(FACILITY_SEARCH_QUERY, query);
        this.startActivity(intent);
    }


//    @Override
    public void openTaggingActivitty() {
        this.startActivity(new Intent(this,FacilityTaggingActivity.class));
    }

}
