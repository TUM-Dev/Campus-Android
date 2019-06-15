package de.tum.in.tumcampusapp.component.ui.ticket.activity;

import android.os.Bundle;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.other.generic.activity.BaseActivity;

public class EventsActivity extends BaseActivity {

    public EventsActivity() {
        super(R.layout.activity_events);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.contentFrame, EventsFragment.newInstance())
                    .commit();
        }
    }

}


