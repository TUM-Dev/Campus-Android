package de.tum.in.tumcampusapp.component.ui.ticket.activity;

import android.os.Bundle;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.other.generic.activity.BaseActivity;
import de.tum.in.tumcampusapp.component.ui.ticket.fragment.EventDetailsFragment;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Event;
import de.tum.in.tumcampusapp.utils.Const;


/**
 * This Activity displays more information about the event
 * Especially the full description, link and the full dates
 * Navigates back to EventsActivity or opens BuyTicketActivity
 */
public class EventDetailsActivity extends BaseActivity {

    public EventDetailsActivity() {
        super(R.layout.activity_event_details);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Event event = getIntent().getParcelableExtra(Const.KEY_EVENT);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, EventDetailsFragment.newInstance(event))
                .commit();
    }

}
