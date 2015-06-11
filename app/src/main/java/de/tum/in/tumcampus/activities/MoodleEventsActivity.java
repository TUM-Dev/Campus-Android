package de.tum.in.tumcampus.activities;


import android.app.ProgressDialog;
import android.content.DialogInterface;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.generic.ActivityForDownloadingExternal;
import de.tum.in.tumcampus.adapters.MoodleEventAdapter;
import de.tum.in.tumcampus.auxiliary.DateUtils;
import de.tum.in.tumcampus.models.MoodleEvent;
import de.tum.in.tumcampus.models.MoodleToken;
import de.tum.in.tumcampus.models.managers.MoodleManager;
import de.tum.in.tumcampus.models.managers.MoodleUpdateListViewDelegate;
import de.tum.in.tumcampus.models.managers.RealMoodleManager;

/**
 * Created by a2k on 6/8/2015.
 * Activity for showing events of a user in moodle
 */
public class MoodleEventsActivity extends ActivityForDownloadingExternal implements AdapterView.OnItemClickListener, MoodleUpdateListViewDelegate {

    MoodleManager realManager;
    ProgressDialog mDialog;

    Intent intent;

    private String userToken;
    private RecyclerView eventsRecyclerView;
    private RecyclerView.LayoutManager eventsLayoutManager;
    private RecyclerView.Adapter eventsAdapter;
    ArrayList<MoodleEvent> userEvents;

    public MoodleEventsActivity() {
        super("MoodleEvents", R.layout.activity_moodle_events);
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        baseSetUp();
        mDialog.show();
        realManager.requestUserEvents(this);

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

            MoodleEvent event = userEvents.get(position);
            int duration = event.getTimeduration();

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

    /**
     * This method populates the data for lists which will be shown
     * on this actiivty.
     * the data is retrieved from moodleManager
     *
     */
    public void refreshListView() {

        userEvents = new ArrayList<MoodleEvent>(realManager.getUserEvents());
        eventsAdapter = new MoodleEventAdapter(userEvents, this);
        eventsRecyclerView.setAdapter(eventsAdapter);
        eventsAdapter.notifyDataSetChanged();
        mDialog.dismiss();

    }

    /**
     * Base setup for the Activity. All local variables are initialized here
     */
    private void baseSetUp() {
        mDialog = new ProgressDialog(this);
        mDialog.setMessage(getResources().getString(R.string.loading));
        mDialog.setCancelable(true);
        mDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        intent = getIntent();
        userToken = intent.getStringExtra("user_token");
        realManager = new RealMoodleManager(this);
        realManager.setMoodleUserToken(new MoodleToken());
        realManager.getMoodleUserToken().setToken(userToken);

        eventsRecyclerView = (RecyclerView) findViewById(R.id.moodleEventList);
        eventsLayoutManager = new LinearLayoutManager(this);
        eventsRecyclerView.setLayoutManager(eventsLayoutManager);

    }


}
