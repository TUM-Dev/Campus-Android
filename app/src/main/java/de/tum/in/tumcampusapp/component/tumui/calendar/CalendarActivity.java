package de.tum.in.tumcampusapp.component.tumui.calendar;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.other.generic.activity.ActivityForAccessingTumOnline;
import de.tum.in.tumcampusapp.component.tumui.calendar.model.EventsResponse;
import de.tum.in.tumcampusapp.utils.Const;

/**
 * Activity showing the user's calendar. Calendar items (events) are fetched from TUMOnline and displayed as blocks on a timeline.
 */
public class CalendarActivity extends ActivityForAccessingTumOnline<EventsResponse> {

    public CalendarActivity() {
        super(R.layout.activity_calendar);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            long showDate = getIntent().getLongExtra(Const.EVENT_TIME, -1);
            String eventId = getIntent().getStringExtra(Const.KEY_EVENT_ID);
            Fragment fragment = CalendarFragment.newInstance(showDate, eventId);

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.contentFrame, fragment)
                    .commit();
        }
    }

}
