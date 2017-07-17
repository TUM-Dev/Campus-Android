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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.tum.in.tumcampusapp.R;

import de.tum.in.tumcampusapp.activities.generic.ActivityForSearching;
import de.tum.in.tumcampusapp.api.TUMCabeClient;
import de.tum.in.tumcampusapp.auxiliary.FacilityLocatorSuggestionProvider;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.models.tumcabe.Facility;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity to fetch and display the curricula of different study programs.
 */
public class FacilityActivity extends ActivityForSearching implements OnItemClickListener {

    private Map<String, Facility> options;
    private ArrayAdapter<String> arrayAdapter;

    public FacilityActivity() {
        super(R.layout.activity_facility, FacilityLocatorSuggestionProvider.AUTHORITY,3);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Sets the adapter
        ListView list = (ListView) this.findViewById(R.id.activity_facility_list_view);
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        list.setAdapter(arrayAdapter);
        list.setOnItemClickListener(this);

        Integer categoryId = getIntent().getExtras().getInt(FacilityMainActivity.FACILITY_CATEGORY_ID);
        String facilitySearchQuery = getIntent().getExtras().getString(FacilityMainActivity.FACILITY_SEARCH_QUERY);

        if(categoryId!=null && categoryId!=0){
            setTitle("Category : "+getIntent().getExtras().getString(FacilityMainActivity.FACILITY_CATEGORY_NAME));
            fetchFacilitiesByCategory(categoryId);
        }else if(facilitySearchQuery!=null){
            setTitle("Search : "+facilitySearchQuery);
            fetchFacilitiesByQuery(facilitySearchQuery);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void fetchFacilitiesByQuery(String query) {
        try {
        TUMCabeClient.getInstance(this).getFacilitiesByQuery(query,new Callback<List<Facility>>() {
                @Override
                public void onResponse(Call<List<Facility>> call, Response<List<Facility>> response) {
                    if (!response.isSuccessful()) {
                        Utils.logv("Error getting facility categories: " + response.message());
                        return;
                    }
                    List<Facility> facilities=response.body();
                    if(facilities!=null && facilities.size()>0){
                        arrayAdapter.clear();
                        options = new HashMap<>();
                        for (Facility facility: facilities) {
                            arrayAdapter.add(facility.getName());
                            options.put(facility.getName(), facility);
                        }
                    }
                }

                @Override
                public void onFailure(Call<List<Facility>> call, Throwable throwable) {
                    Utils.log(throwable, "Failure getting facility categories from the server");
                    Utils.showToastOnUIThread(FacilityActivity.this, R.string.facility_categories_failure);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void fetchFacilitiesByCategory(Integer categoryId) {
        try {
            TUMCabeClient.getInstance(this).getFacilitiesByCategory(categoryId,new Callback<List<Facility>>() {
                @Override
                public void onResponse(Call<List<Facility>> call, Response<List<Facility>> response) {
                    if (!response.isSuccessful()) {
                        Utils.logv("Error getting facility categories: " + response.message());
                        return;
                    }
                    List<Facility> facilities=response.body();
                    if(facilities!=null && facilities.size()>0){
                        arrayAdapter.clear();
                        options = new HashMap<>();
                        for (Facility facility: facilities) {
                            arrayAdapter.add(facility.getName());
                            options.put(facility.getName(), facility);
                        }
                    }
                }

                @Override
                public void onFailure(Call<List<Facility>> call, Throwable throwable) {
                    Utils.log(throwable, "Failure getting facility categories from the server");
                    Utils.showToastOnUIThread(FacilityActivity.this, R.string.facility_categories_failure);
                }
            });
        } catch (IOException e) {
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
        String facilityName = ((TextView) view).getText().toString();

        // Puts URL and name into an intent and starts the detail view

        Intent intent = new Intent(this, FacilityDisplayActivity.class);
        Facility facility=options.get(facilityName);
        Bundle bundle=new Bundle();
        bundle.putSerializable(FacilityDisplayActivity.FACILITY,facility);
        intent.putExtras(bundle);

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
        fetchFacilitiesByQuery(query);
    }
}
