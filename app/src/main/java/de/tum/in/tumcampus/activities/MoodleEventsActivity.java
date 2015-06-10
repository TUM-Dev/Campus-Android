package de.tum.in.tumcampus.activities;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.generic.ActivityForDownloadingExternal;
import de.tum.in.tumcampus.adapters.MoodleEventAdapter;
import de.tum.in.tumcampus.auxiliary.DateUtils;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.MoodleEvent;
import de.tum.in.tumcampus.models.managers.MockMoodleManager;
import de.tum.in.tumcampus.models.managers.MoodleManager;
import de.tum.in.tumcampus.trace.Util;

/**
 * Created by a2k on 6/8/2015.
 * Activity for showing events of a user in moodle
 */
public class MoodleEventsActivity extends ActivityForDownloadingExternal implements AdapterView.OnItemClickListener {

    MoodleManager moodleManager;
    private RecyclerView eventsRecyclerView;
    private RecyclerView.LayoutManager eventsLayoutManager;
    private RecyclerView.Adapter eventsAdapter;

    public MoodleEventsActivity() {
        super("MoodleEvents", R.layout.activity_moodle_events);
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        // creating mockMoodleManager
        moodleManager = new MockMoodleManager();

        eventsRecyclerView = (RecyclerView) findViewById(R.id.moodleEventList);
        eventsLayoutManager = new LinearLayoutManager(this);
        eventsRecyclerView.setLayoutManager(eventsLayoutManager);

        //getting events from moodle manager
        List<MoodleEvent> user_events = moodleManager.getUserEvents();
        eventsAdapter = new MoodleEventAdapter(user_events, this);
        eventsRecyclerView.setAdapter(eventsAdapter);

        Utils.log(user_events.toString());

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_moodle, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.moodle_my_courses:
                Intent coursesIntent = new Intent(this,MoodleMainActivity.class);
                startActivity(coursesIntent);
                return true;
            case R.id.events:
                // Do nothing
                return true;
        }
        return false;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

       //TODO do something for the previous versions of android
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            TextView name = (TextView) findViewById(R.id.event_title);
            TextView description_view = (TextView) findViewById(R.id.event_description);
            TextView date_view = (TextView) findViewById(R.id.event_date);

            String event_name = name.getText().toString();
            String event_desc = description_view.getText().toString();
            String dateText = date_view.getText().toString();

            int duration = MoodleEvent.getDuration(dateText);
            Date date = DateUtils.parseSimpleDateFormat(dateText);
            GregorianCalendar begin = new GregorianCalendar();
            begin.setTime(date);

            Intent calendarIntent = new Intent(Intent.ACTION_INSERT);
            calendarIntent.setData(CalendarContract.Events.CONTENT_URI);
            calendarIntent.putExtra(CalendarContract.Events.TITLE, event_name);
            calendarIntent.putExtra(CalendarContract.Events.DESCRIPTION, event_desc);
            calendarIntent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, begin.getTimeInMillis());

            // making the end of the event
            begin.add(GregorianCalendar.SECOND, duration);
            calendarIntent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, begin.getTimeInMillis());
            startActivity(calendarIntent);
        }
    }
}
