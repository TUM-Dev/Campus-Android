package de.tum.in.tumcampusapp.activities;

import android.os.Bundle;

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
        super(R.layout.activity_barrier_free_list_info);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        listview = (StickyListHeadersListView) findViewById(R.id.activity_barrier_info_list_view);

        // request contact from database
        request = new TUMBarrierFreeRequest(this);
        startLoading();
    }

    @Override
    protected List<BarrierfreeContact> onLoadInBackground(Void... arg) {
        return request.fetchResponsiblePersonList();
    }

    @Override
    protected void onLoadFinished(List<BarrierfreeContact> result) {
        contacts = result;
        adapter = BarrierfreeContactAdapter.newAdapter(this, contacts);
        listview.setAdapter(adapter);
    }
}
