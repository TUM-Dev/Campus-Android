package de.tum.in.tumcampusapp.activities;

import android.os.Bundle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.ActivityForLoadingInBackground;
import de.tum.in.tumcampusapp.adapters.BarrierfreeContactAdapter;
import de.tum.in.tumcampusapp.api.TUMCabeClient;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.models.tumcabe.BarrierfreeContact;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class BarrierFreeContactActivity extends ActivityForLoadingInBackground<Void, List<BarrierfreeContact>> {

    public StickyListHeadersListView listview;
    private List<BarrierfreeContact> contacts;
    private BarrierfreeContactAdapter adapter;

    public BarrierFreeContactActivity() {
        super(R.layout.activity_barrier_free_list_info);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listview = findViewById(R.id.activity_barrier_info_list_view);
        startLoading();
    }

    @Override
    protected List<BarrierfreeContact> onLoadInBackground(Void... arg) {
        showLoadingStart();
        List<BarrierfreeContact> result = new ArrayList<>();
        try {
            result = TUMCabeClient.getInstance(this)
                                  .getBarrierfreeContactList();
        } catch (IOException e) {
            Utils.log(e);
            return result;
        }
        return result;
    }

    @Override
    protected void onLoadFinished(List<BarrierfreeContact> result) {
        showLoadingEnded();
        if (result == null || result.isEmpty()) {
            showErrorLayout();
            return;
        }
        contacts = result;
        adapter = new BarrierfreeContactAdapter(this, contacts);
        listview.setAdapter(adapter);
    }
}
