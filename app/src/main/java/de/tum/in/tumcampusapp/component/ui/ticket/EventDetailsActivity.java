package de.tum.in.tumcampusapp.component.ui.ticket;

import android.os.Bundle;
import android.support.v4.view.ViewPager;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.other.generic.activity.BaseActivity;
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

        // TODO: replace by real data -> for now, using static method with mock data for testing purposes
        EventAdapter eventAdapter = new EventAdapter(getSupportFragmentManager(), EventsController.getEvents());
        mPager.setAdapter(eventAdapter);
        mPager.setCurrentItem(0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposable.clear();
    }
}
