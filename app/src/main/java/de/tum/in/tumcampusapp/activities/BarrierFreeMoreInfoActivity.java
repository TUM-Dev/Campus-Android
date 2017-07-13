package de.tum.in.tumcampusapp.activities;

import android.os.Bundle;

import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.ActivityForLoadingInBackground;
import de.tum.in.tumcampusapp.models.barrierfree.BarrierfreeMoreInfo;

public class BarrierFreeMoreInfoActivity extends ActivityForLoadingInBackground<Void, List<BarrierfreeMoreInfo>> {


    public BarrierFreeMoreInfoActivity(){
        super(R.layout.activity_barrier_free_list_info);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
    }

    @Override
    protected void onLoadFinished(List<BarrierfreeMoreInfo> result) {

    }

    @Override
    protected List<BarrierfreeMoreInfo> onLoadInBackground(Void... arg) {
        return null;
    }
}
