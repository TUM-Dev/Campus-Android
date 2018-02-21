package de.tum.in.tumcampusapp.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.BaseActivity;
import de.tum.in.tumcampusapp.fragments.PlansViewFragment;

/**
 * Activity to show plans.
 */

public class PlansActivity extends BaseActivity {
    public PlansActivity() {
        super(R.layout.activity_plans);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PlansViewFragment newFragment = new PlansViewFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.activity_plans_fragment_frame, newFragment);
        transaction.commit();
    }
}

