package de.tum.in.tumcampus.activities;

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
import de.tum.in.tumcampus.activities.generic.ProgressActivity;
import de.tum.in.tumcampus.adapters.MoodleEventAdapter;
import de.tum.in.tumcampus.auxiliary.DateUtils;
import de.tum.in.tumcampus.auxiliary.NetUtils;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.MoodleEvent;
import de.tum.in.tumcampus.models.managers.MoodleManager;
import de.tum.in.tumcampus.models.managers.MoodleUpdateDelegate;
import de.tum.in.tumcampus.models.managers.RealMoodleManager;

/**
 * Created by a2k on 6/8/2015.
 * Activity for showing events of a user in moodle
 */
public class MoodleEventsActivity extends ProgressActivity implements AdapterView.OnItemClickListener, MoodleUpdateDelegate {

    MoodleManager realManager;

    private RecyclerView eventsRecyclerView;
    ArrayList<MoodleEvent> userEvents;

    public MoodleEventsActivity() {
        super(R.layout.activity_moodle_events);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        baseSetUp();
        showLoadingStart();
        realManager.requestUserEvents(this);
    }


    @Override
    public void onRestart() {
        super.onRestart();
        realManager.requestUserEvents(this);
    }

    @Override
    public void onRefresh() {
        /* this method is called when the internet connection
        is back
         */
        realManager.requestUserEvents(this);
        showLoadingStart();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_moodle, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.moodle_my_courses:
                Intent coursesIntent = new Intent(this, MoodleMainActivity.class);
                startActivity(coursesIntent);
                finish();
                return true;
            case R.id.events:
                // Do nothing
                Utils.showToast(this, R.string.moodle_stay_here);
                return true;
        }
        return false;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        // start the calendar application if the sdk version of the device is OK
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            TextView name = (TextView) findViewById(R.id.event_title);
            TextView description_view = (TextView) findViewById(R.id.event_description);
            TextView date_view = (TextView) findViewById(R.id.event_date);

            String event_name = name.getText().toString();
            String event_desc = description_view.getText().toString();
            String dateText = date_view.getText().toString();

            int duration = userEvents.get(position).getTimeduration();
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
     */
    public void refresh() {

        try {
            if (!NetUtils.isConnected(this)) {
                Utils.showToast(this, R.string.no_internet_connection);
                showNoInternetLayout();
                return;
            }
            userEvents = new ArrayList<>(realManager.getUserEvents());
            RecyclerView.Adapter eventsAdapter = new MoodleEventAdapter(userEvents, this);
            eventsRecyclerView.setAdapter(eventsAdapter);
            eventsAdapter.notifyDataSetChanged();
            showLoadingEnded();
            //mDialog.dismiss();
        } catch (Exception e) {
            Utils.log(e, "events activity getting refresh failed");
            Utils.showToast(this, getString(R.string.error_something_wrong));
            showLoadingEnded();
        }

    }

    /**
     * Base setup for the Activity. All local variables are initialized here
     */
    private void baseSetUp() {
        /*
        mDialog = new ProgressDialog(this);
        mDialog.setMessage(getResources().getString(R.string.loading));
        mDialog.setCancelable(true);
        mDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        */
        realManager = RealMoodleManager.getInstance(this, this);

        eventsRecyclerView = (RecyclerView) findViewById(R.id.moodleEventList);
        RecyclerView.LayoutManager eventsLayoutManager = new LinearLayoutManager(this);
        eventsRecyclerView.setLayoutManager(eventsLayoutManager);

    }


}
