package de.tum.in.tumcampusapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.FacilityActivity;
import de.tum.in.tumcampusapp.api.TUMCabeClient;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.models.tumcabe.FacilityCategory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by shayansiddiqui on 30.06.17.
 */

public class FacilityCategoriesTabFrame extends Fragment implements AdapterView.OnItemClickListener {

    public static final String FACILITY_CATEGORY_ID = "category_id";
    public static final String FACILITY_CATEGORY_NAME = "category_name";

    private Map<String, Integer> options;
    private ArrayAdapter<String> arrayAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view=inflater.inflate(R.layout.activity_facility_categories_tab, container, false);

        // Sets the adapter
        ListView list = (ListView) view.findViewById(R.id.activity_facility_categories_list_view);
        arrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1);
        list.setAdapter(arrayAdapter);
        list.setOnItemClickListener(this);
        createCategoriesList();
        return view;
    }

    private void createCategoriesList() {
        try {
            TUMCabeClient.getInstance(getActivity()).getFacilityCategories(new Callback<List<FacilityCategory>>() {
                @Override
                public void onResponse(Call<List<FacilityCategory>> call, Response<List<FacilityCategory>> response) {
                    if (!response.isSuccessful()) {
                        Utils.logv("Error getting facility categories: " + response.message());
                        return;
                    }
                    List<FacilityCategory> facilityCategories=response.body();
                    if(facilityCategories!=null){
                        options = new HashMap<>();
                        for(FacilityCategory fc:facilityCategories){
                            arrayAdapter.add(fc.getName());
                            options.put(fc.getName(), fc.getId());
                        }
                    }
                }

                @Override
                public void onFailure(Call<List<FacilityCategory>> call, Throwable throwable) {
                    Utils.log(throwable, "Failure getting facility categories from the server");
                    Utils.showToastOnUIThread(getActivity(), R.string.facility_categories_failure);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        String categoryName = ((TextView) view).getText().toString();
        Intent intent = new Intent(getActivity(), FacilityActivity.class);
        intent.putExtra(FACILITY_CATEGORY_ID, options.get(categoryName));
        intent.putExtra(FACILITY_CATEGORY_NAME,categoryName);
        this.startActivity(intent);
        
    }
}
