package de.tum.in.tumcampusapp.component.ui.barrierfree;

import android.os.Bundle;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.other.generic.activity.BaseActivity;

public class BarrierFreeInfoActivity extends BaseActivity {

    public BarrierFreeInfoActivity() {
        super(R.layout.activity_barrier_free_info);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.contentFrame, BarrierFreeInfoFragment.newInstance())
                    .commit();
        }
    }

}
