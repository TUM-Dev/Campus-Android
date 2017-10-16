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
import de.tum.in.tumcampusapp.activities.FacilityTaggingActivity;
import de.tum.in.tumcampusapp.api.TUMCabeClient;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.exceptions.NoPrivateKey;
import de.tum.in.tumcampusapp.models.tumcabe.ChatMember;
import de.tum.in.tumcampusapp.models.tumcabe.ChatVerification;
import de.tum.in.tumcampusapp.models.tumcabe.Facility;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by shayansiddiqui on 30.06.17.
 */

public class FacilityMyFacilitiesTabFrame extends Fragment implements AdapterView.OnItemClickListener {

    private Map<String, Facility> options;
    private ArrayAdapter<String> arrayAdapter;
    private String lrzId;


    @Override
    public void onResume() {
        super.onResume();
        createMyFacilitiesList();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view=inflater.inflate(R.layout.activity_facility_my_facilities_tab, container, false);
        ListView list = (ListView) view.findViewById(R.id.activity_facility_my_facilities_list_view);
        arrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1);
        list.setAdapter(arrayAdapter);
        list.setOnItemClickListener(this);
        lrzId=Utils.getSetting(getActivity(), Const.LRZ_ID, "");
        createMyFacilitiesList();
        return view;
    }

    private void createMyFacilitiesList() {
        try {
            final ChatVerification v = new ChatVerification(getContext(), Utils.getSetting(getContext(), Const.CHAT_MEMBER, ChatMember.class));
            TUMCabeClient.getInstance(getActivity()).getMyFacilities(v,new Callback<List<Facility>>() {
                @Override
                public void onResponse(Call<List<Facility>> call, Response<List<Facility>> response) {
                    if (!response.isSuccessful()) {
                        Utils.logv("Error getting my facilities: " + response.message());
                        return;
                    }
                    List<Facility> facilities=response.body();
                    arrayAdapter.clear();
                    if(facilities!=null && facilities.size()>0){
                        options = new HashMap<>();
                        for (Facility facility: facilities) {
                            arrayAdapter.add(facility.getName());
                            options.put(facility.getName(), facility);
                        }
                    }
                }
                @Override
                public void onFailure(Call<List<Facility>> call, Throwable throwable) {
                    Utils.log(throwable, "Failure getting my facilities");
                    Utils.showToastOnUIThread(getActivity(), R.string.facility_my_facilities_error);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoPrivateKey noPrivateKey) {
            noPrivateKey.printStackTrace();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String facilityName = ((TextView) view).getText().toString();
        // Puts URL and name into an intent and starts the detail view
        Intent intent = new Intent(getActivity(), FacilityTaggingActivity.class);
        Facility facility=options.get(facilityName);
        Bundle bundle=new Bundle();
        bundle.putSerializable(FacilityTaggingActivity.FACILITY,facility);
        bundle.putBoolean(FacilityTaggingActivity.EDIT_MODE, true);
        intent.putExtras(bundle);
        this.startActivity(intent);
    }
}
