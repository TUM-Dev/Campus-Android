package de.tum.in.tumcampusapp.component.ui.ticket.activity;

import android.os.Bundle;
import android.support.v4.view.ViewPager;

import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.other.generic.activity.BaseActivity;
import de.tum.in.tumcampusapp.component.ui.ticket.EventsController;
import de.tum.in.tumcampusapp.component.ui.ticket.adapter.EventDetailsAdapter;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Event;


public class EventDetailsActivity extends BaseActivity {

    public EventDetailsActivity() {
        super(R.layout.activity_event_details);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventsController eventsController = new EventsController(this);
        List<Event> events = eventsController.getEvents();

        EventDetailsAdapter eventDetailsAdapter =
                new EventDetailsAdapter(getSupportFragmentManager(), events);

        ViewPager viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(eventDetailsAdapter);

        int margin = getResources().getDimensionPixelOffset(R.dimen.material_default_padding);
        viewPager.setPageMargin(margin);

        Event event = getIntent().getParcelableExtra("event");
        int startIndex = events.indexOf(event);

        viewPager.setCurrentItem(startIndex);
    }

}
