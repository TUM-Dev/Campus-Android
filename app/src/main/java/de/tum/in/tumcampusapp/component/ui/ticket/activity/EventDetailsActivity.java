package de.tum.in.tumcampusapp.component.ui.ticket.activity;

import android.os.Bundle;
import android.support.v4.view.ViewPager;

import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.other.generic.activity.BaseActivity;
import de.tum.in.tumcampusapp.component.ui.ticket.adapter.EventDetailsAdapter;
import de.tum.in.tumcampusapp.component.ui.ticket.EventsController;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Event;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;


public class EventDetailsActivity extends BaseActivity {

    public EventDetailsActivity() {
        super(R.layout.activity_kino);
    }

    private final Disposable disposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventsController eventsController = new EventsController(this);
        List<Event> events = eventsController.getEvents();

        EventDetailsAdapter eventDetailsAdapter =
                new EventDetailsAdapter(getSupportFragmentManager(), events);

        ViewPager mPager = findViewById(R.id.pager);
        mPager.setAdapter(eventDetailsAdapter);

        Event event = getIntent().getParcelableExtra("event");
        int startIndex = events.indexOf(event);

        /*
        int clickedEventId = getIntent().getIntExtra("event_id", -1);
        int startPosition = 0;
        for (int i = 0; i < events.size(); i++) {
            Event event = events.get(i);
            if (event.getId() == clickedEventId) {
                startPosition = i;
                break;
            }
        }
        */

        mPager.setCurrentItem(startIndex);
    }

    @Override
    protected void onStop() {
        super.onStop();
        disposable.dispose();
    }

}
