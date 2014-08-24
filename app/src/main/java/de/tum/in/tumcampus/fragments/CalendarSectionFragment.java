package de.tum.in.tumcampus.fragments;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.RoomFinderActivity;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.managers.CalendarManager;

/**
 * Fragment for each calendar-page.
 */
public class CalendarSectionFragment extends Fragment {
    private Activity activity;

    private CalendarManager calendarManager;
    private Date today = new Date();
    private RelativeLayout mainScheduleLayout;

    private final ArrayList<RelativeLayout> eventList = new ArrayList<RelativeLayout>();
    private int[][] eventTimes;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //Useful stuff
        activity = getActivity();
        calendarManager = new CalendarManager(activity);

        //Inflate the view for today only
        View rootView = inflater.inflate(R.layout.fragment_calendar_section, container, false);

        // Parse the date we want to show events for
        String date = getArguments().getString("date");
        today = Utils.getDateTimeISO(date);

        // Scroll to a default position
        final ScrollView scrollview = ((ScrollView) rootView.findViewById(R.id.scrollview));
        scrollview.post(new Runnable() {
            @Override
            public void run() {
                scrollview.scrollTo(0, (int) getResources().getDimension(R.dimen.default_scroll_position));
            }
        });

        // Make the event items clickable
        mainScheduleLayout = (RelativeLayout) rootView.findViewById(R.id.main_schedule_layout);
        mainScheduleLayout.setClickable(true);

        // Add the entries when layout is displayed, thus not blocking
        mainScheduleLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                // Ensure we call it only once
                if (Build.VERSION.SDK_INT < 16) {
                    mainScheduleLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    mainScheduleLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }

                // Fetch entries and add them to our view
                updateCalendarView();
            }
        });


        return rootView;
    }

    private void parseEvents() {
        Date dateStart;
        Date dateEnd;
        float start;
        float end;
        float duration;

        // Our model giving us the needed data
        Cursor cursor = calendarManager.getFromDbForDate(today);

        // Some useful vars
        LayoutInflater layoutInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout eventView;
        int oneHourHeight = ((int) activity.getResources().getDimension(R.dimen.time_one_hour));

        //Remember durations (start/end) to detect overlapping events
        this.eventTimes = new int[cursor.getCount()][2];
        int event = 0;

        //Iterate through everything
        while (cursor.moveToNext()) {
            //Fetch some data
            final String status = cursor.getString(1);
            final String strStart = cursor.getString(5);
            final String strEnd = cursor.getString(6);

            //Check if event is possibly canceled
            if (!status.equals("CANCEL")) {
                //Create our layout_time_entry
                eventView = (RelativeLayout) layoutInflater.inflate(R.layout.layout_time_entry, null);

                //Calculate the duration
                dateStart = Utils.getISODateTime(strStart);
                dateEnd = Utils.getISODateTime(strEnd);

                start = dateStart.getHours() * 60 + dateStart.getMinutes();
                end = dateEnd.getHours() * 60 + dateStart.getMinutes();

                duration = (end - start) / 60f;

                // Height is determined by duration / Width will be handled later
                LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Math.round(duration * oneHourHeight));

                // Offset by top margin until start time is reached
                params.setMargins(0, Math.round(oneHourHeight * (start / 60f)), 0, 0);

                //Check if room is present and set text accordingly
                String room = cursor.getString(7);
                if (room != null && room.length() != 0) {
                    setText(eventView, cursor.getString(3) + " / " + cursor.getString(7));
                    eventView.setTag(cursor.getString(7));
                } else {
                    setText(eventView, cursor.getString(3));
                }

                eventView.setLayoutParams(params);
                eventList.add(eventView);

                //Remember times for overlap check
                eventTimes[event][0] = Math.round(start);
                eventTimes[event][1] = Math.round(end);
            }
        }
    }

    private void setText(RelativeLayout entry, String text) {
        TextView textView = (TextView) entry.findViewById(R.id.entry_title);
        textView.setText(text);
    }

    private void updateCalendarView() {

        //Clear previous stuff
        eventList.clear();
        mainScheduleLayout.removeAllViews();

        //Get the new stuff
        parseEvents();

        //Get the width of the RL where events will be added
        float avaibleSpace = mainScheduleLayout.getWidth() - getResources().getDimension(R.dimen.padding_large) * 2;

        //Useful vars
        int currentEvent = 0;
        boolean trippleCollision = false;

        //Iterate through all items
        for (RelativeLayout event : eventList) {
            //Adapt width and offset when colliding events are added
            //Check previous item
            boolean collisionPrev = false, collisionNext = false;
            if (currentEvent > 0 && overlap(eventTimes[currentEvent][0], eventTimes[currentEvent][1], eventTimes[currentEvent - 1][0], eventTimes[currentEvent - 1][1])) {
                collisionPrev = true;
            }

            //Check next item
            if (currentEvent < (eventTimes.length - 1) && overlap(eventTimes[currentEvent][0], eventTimes[currentEvent][1], eventTimes[currentEvent + 1][0], eventTimes[currentEvent + 1][1])) {
                collisionNext = true;
            }

            //Fetch current parameters
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) event.getLayoutParams();

            // When collision detected: set width and offset
            if (collisionPrev == true && collisionNext == false) {
                //When three events overlap we need to double the offset
                if (trippleCollision) {
                    trippleCollision = false;

                    params.width = Math.round((float) avaibleSpace / 3);
                    params.setMargins(2 * params.width, params.topMargin, params.rightMargin, params.bottomMargin);
                } else {
                    params.width = Math.round((float) avaibleSpace / 2);
                    params.setMargins(params.width, params.topMargin, params.rightMargin, params.bottomMargin);
                }
            } else if (collisionPrev == false && collisionNext == true) {

                params.width = Math.round((float) avaibleSpace / 2);
                params.setMargins(0, params.topMargin, params.rightMargin, params.bottomMargin);

            } else if (collisionPrev == true && collisionNext == true) {
                //Set previous
                RelativeLayout prevEvent=eventList.get(currentEvent - 1);
                RelativeLayout.LayoutParams prevLp = (RelativeLayout.LayoutParams) prevEvent.getLayoutParams();
                prevLp.width = Math.round((float) avaibleSpace / 3);
                prevEvent.setLayoutParams(prevLp);

                //Set current
                params.width = Math.round((float) avaibleSpace / 3);
                params.setMargins(params.width, params.topMargin, params.rightMargin, params.bottomMargin);

                //Tell next one
                trippleCollision = true;
            }

            //Update params
            event.setLayoutParams(params);
            currentEvent++;

            //Add to main view and setup click to room finder listener
            mainScheduleLayout.addView(event);
            this.Listener(event);
        }
    }

    private boolean overlap(long startTime1, long endTime1, long startTime2, long endTime2) {
        if (endTime1 < startTime2 || startTime1 > endTime2) {
            return false;
        }
        return true;
    }

    /**
     * Setup an click listener which is connected to the room finder to locate rooms of the shown lectures
     *
     * @param v View to bind to
     */
    private void Listener(View v) {
        v.setClickable(true);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //try to get the location of the lectures: sometimes none is provided, then exit gracefully
                String room = (String) v.getTag();
                if (room == null) {
                    return;
                }
                final String strList[] = room.split(",");

                //Launch the roomfinder activity
                Intent i = new Intent(activity, RoomFinderActivity.class);
                i.setAction(Intent.ACTION_SEARCH);
                i.putExtra(SearchManager.QUERY, strList[0]);
                activity.startActivity(i);
            }
        });
    }
}