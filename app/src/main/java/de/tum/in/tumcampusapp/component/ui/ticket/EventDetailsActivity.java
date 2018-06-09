package de.tum.in.tumcampusapp.component.ui.ticket;

import android.os.Bundle;
import android.support.v4.view.ViewPager;

import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.other.generic.activity.BaseActivity;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Event;
import io.reactivex.disposables.CompositeDisposable;

/**
 * TODO: combine this with KinoActivity
 */
public class EventDetailsActivity extends BaseActivity{

    public EventDetailsActivity() {
        super(R.layout.activity_kino);
    }

    private final CompositeDisposable disposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set up ViewPager and adapter
        ViewPager mPager = findViewById(R.id.pager);

        // TODO: extract key to const class
        int clickedEventId = getIntent().getIntExtra("event_id", 0);

        // TODO: replace by real data -> for now, using static method with mock data for testing purposes
        List<Event> events = EventsController.getEvents();
        EventDetailsAdapter eventDetailsAdapter = new EventDetailsAdapter(getSupportFragmentManager(),
                events);

        mPager.setAdapter(eventDetailsAdapter);

        // Use clickedEventId to show the clicked event in the EventDetailsView
        int startPosition = 0;
        for(int i = 0; i < events.size(); i++){
            Event event = events.get(i);
            if (event.getId() == clickedEventId){
                startPosition = i;
                break;
            }
        }

        mPager.setCurrentItem(startPosition);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposable.clear();
    }
}
