package de.tum.in.tumcampusapp.activities;

import android.os.Bundle;

import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.BaseActivity;
import de.tum.in.tumcampusapp.adapters.BarrierfreeContactAdapter;
import de.tum.in.tumcampusapp.models.barrierfree.BarrierfreeContact;
import de.tum.in.tumcampusapp.tumonline.TUMBarrierFreeContactsRequestFetchListener;
import de.tum.in.tumcampusapp.tumonline.TUMBarrierFreeRequest;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class BarrierFreeContactActivity extends BaseActivity implements TUMBarrierFreeContactsRequestFetchListener{

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

        listview = (StickyListHeadersListView) findViewById(R.id.activity_barrier_free_person_list_view);

        request = new TUMBarrierFreeRequest(this);
        request.fetchContactsInteractive(this, this);
    }

    @Override
    /**
     * Fetch the result. Callback fromTUMBarrierFreeRequest.
     */
    public void onFetch(List<BarrierfreeContact> result) {
        contacts = result;
        adapter = BarrierfreeContactAdapter.newAdapter(this, contacts);
        listview.setAdapter(adapter);
    }

    @Override
    public void onFetchError(String errorReason) {
        request.cancelContactsRequest(true);
    }

    @Override
    public void onNoInternetError() {

    }
}
