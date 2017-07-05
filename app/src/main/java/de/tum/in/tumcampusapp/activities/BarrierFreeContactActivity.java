package de.tum.in.tumcampusapp.activities;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.BaseActivity;
import de.tum.in.tumcampusapp.adapters.BarrierfreeContactAdapter;
import de.tum.in.tumcampusapp.models.barrierfree.BarrierfreeContact;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class BarrierFreeContactActivity extends BaseActivity {

    public StickyListHeadersListView listview;
    private List<BarrierfreeContact> contacts;
    private BarrierfreeContactAdapter adapter;

    public BarrierFreeContactActivity() {
        super(R.layout.activity_barrier_free_contact);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        listview = (StickyListHeadersListView) findViewById(R.id.activity_barrier_free_person_list_view);
        contacts = new ArrayList<BarrierfreeContact>();
        fetchContacts();

        adapter = BarrierfreeContactAdapter.newAdapter(this, contacts);
        listview.setAdapter(adapter);
    }

    private void fetchContacts(){
        //// TODO: 7/5/2017 fetch from request class
        
    }
}
