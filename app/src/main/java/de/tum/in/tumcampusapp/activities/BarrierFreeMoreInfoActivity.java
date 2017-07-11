package de.tum.in.tumcampusapp.activities;

import android.os.Bundle;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.BaseActivity;

public class BarrierFreeMoreInfoActivity extends BaseActivity{
    public BarrierFreeMoreInfoActivity(){
        super(R.layout.activity_barrier_free_more_info);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
    }
}
