package de.tum.in.tumcampusapp.component.ui.cafeteria.activity;

import android.os.Bundle;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.other.generic.activity.BaseActivity;
import de.tum.in.tumcampusapp.component.ui.cafeteria.fragment.CafeteriaFragment;

public class CafeteriaActivity extends BaseActivity {

    public CafeteriaActivity() {
        super(R.layout.activity_cafeteria);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.contentFrame, CafeteriaFragment.newInstance())
                    .commit();
        }
    }

}
