package de.tum.in.tumcampusapp.activities;

import android.os.Bundle;

import com.google.common.base.Optional;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.ActivityForLoadingInBackground;
import de.tum.in.tumcampusapp.adapters.BarrierfreeContactAdapter;
import de.tum.in.tumcampusapp.models.barrierfree.BarrierfreeContact;
import de.tum.in.tumcampusapp.tumonline.TUMBarrierFreeRequest;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class BarrierFreeContactActivity extends ActivityForLoadingInBackground<Void, List<BarrierfreeContact>> {

    public StickyListHeadersListView listview;
    private List<BarrierfreeContact> contacts;
    private BarrierfreeContactAdapter adapter;
    private TUMBarrierFreeRequest request;

    public BarrierFreeContactActivity() {
        super(R.layout.activity_barrier_free_contact);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        listview = (StickyListHeadersListView) findViewById(R.id.activity_barrier_free_person_list_view);
        request = new TUMBarrierFreeRequest(this);
        startLoading();
    }

    @Override
    protected void onLoadFinished(List<BarrierfreeContact> result) {
        System.out.println("Load Finished!");
        contacts = result;
        adapter = BarrierfreeContactAdapter.newAdapter(this, contacts);
        listview.setAdapter(adapter);
    }

    @Override
    protected List<BarrierfreeContact> onLoadInBackground(Void... arg) {
        return request.fetchResponsiblePersonList();
    }
}
