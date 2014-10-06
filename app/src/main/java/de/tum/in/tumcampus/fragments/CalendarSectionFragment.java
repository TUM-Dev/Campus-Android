package de.tum.in.tumcampus.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import java.util.ArrayList;
import java.util.Date;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.auxiliary.calendar.CalendarController;
import de.tum.in.tumcampus.auxiliary.calendar.DayView;
import de.tum.in.tumcampus.auxiliary.calendar.EventLoader;
import de.tum.in.tumcampus.models.managers.CalendarManager;

/**
 * Fragment for each calendar-page.
 */
public class CalendarSectionFragment extends Fragment {
    private boolean mWeekViewMode = false;
    private final Time mSelectedDay = new Time();
    private Activity activity;

    private CalendarManager calendarManager;
    private Date today = new Date();

    private final ArrayList<RelativeLayout> eventList = new ArrayList<RelativeLayout>();
    private int[][] eventTimes;
    private DayView view;


    public CalendarSectionFragment() {}

    public CalendarSectionFragment(long time, boolean weekViewMode) {
        mWeekViewMode = weekViewMode;
        if (time == 0) {
            mSelectedDay.setToNow();
        } else {
            mSelectedDay.set(time);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //Useful stuff
        activity = getActivity();
        calendarManager = new CalendarManager(activity);

        //Inflate the view for today only
        view = new DayView(getActivity(), CalendarController
                .getInstance(getActivity()), null, new EventLoader(getActivity()), mWeekViewMode?7:1);
        view.setLayoutParams(new ViewSwitcher.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        view.setSelected(mSelectedDay, !DateUtils.isToday(mSelectedDay.toMillis(true)), false);
        view.reloadEvents();

        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && view!=null) {
            view.handleOnResume();
        }
    }

    @SuppressWarnings("deprecation")
    /*private void parseEvents() {
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
                    String title = cursor.getString(3);
                    title = title.replaceAll("[A-Z 0-9(LV\\.Nr\\.)=]+$", "");
                    title = title.replaceAll("\\([A-Z]+[0-9]+\\)", "");

                    setText(eventView, title + " / " + cursor.getString(7));
                    eventView.setTag(cursor.getString(7));
                    Log.e("cal ",title);
                } else {
                    Log.e("cal 2",cursor.getString(3));
                    setText(eventView, cursor.getString(3));
                }

                eventView.setLayoutParams(params);
                eventList.add(eventView);

                Log.e("cal 3"," "+event);
                //Remember times for overlap check
                eventTimes[event][0] = Math.round(start);
                eventTimes[event][1] = Math.round(end);
                event++;
            }
        }
    }*/

    private void setText(RelativeLayout entry, String text) {
        TextView textView = (TextView) entry.findViewById(R.id.entry_title);
        textView.setText(text);
    }
/*
    private void updateCalendarView() {

        //Clear previous stuff
        eventList.clear();
        mainScheduleLayout.removeAllViews();

        //Get the new stuff
        parseEvents();

        //Get the width of the RL where events will be added
        float availableSpace = mainScheduleLayout.getWidth() - getResources().getDimension(R.dimen.padding_large) * 2;

        //Useful vars
        int currentEvent = 0;
        boolean tripleCollision = false;

        //Iterate through all items
        for (RelativeLayout event : eventList) {
            //Adapt width and offset when colliding events are added
            boolean collisionPrevPrev = false,collisionPrev = false, collisionNext = false, collisionNextNext = false;

            //Check previous previous item
            if (currentEvent > 1 && this.overlap(eventTimes[currentEvent][0], eventTimes[currentEvent][1], eventTimes[currentEvent - 2][0], eventTimes[currentEvent - 2][1])) {
                collisionPrevPrev = true;
            } else {
                collisionPrevPrev = false;
            }

            //Check previous item

            if (currentEvent > 0 && this.overlap(eventTimes[currentEvent][0], eventTimes[currentEvent][1], eventTimes[currentEvent - 1][0], eventTimes[currentEvent - 1][1])) {
                collisionPrev = true;
            } else {
                collisionPrev = false;
            }

            //Check next item
            if (currentEvent < (eventTimes.length - 1) && this.overlap(eventTimes[currentEvent][0], eventTimes[currentEvent][1], eventTimes[currentEvent + 1][0], eventTimes[currentEvent + 1][1])) {
                collisionNext = true;
            } else {
                collisionNext = false;
            }

            //Check next next item
            if (currentEvent < (eventTimes.length - 2) && this.overlap(eventTimes[currentEvent][0], eventTimes[currentEvent][1], eventTimes[currentEvent + 2][0], eventTimes[currentEvent + 2][1])) {
                collisionNextNext = true;
            } else {
                collisionNextNext = false;
            }

            Log.e("Cal", ""+collisionPrev+" "+collisionNext+ " "+eventTimes.length+" "+currentEvent +" "+ Arrays.toString(eventTimes));

            //Fetch current parameters
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) event.getLayoutParams();

            // When collision detected: set width and offset
            if (collisionPrev && !collisionNext || collisionPrev && collisionPrevPrev) {
                //When three events overlap we need to double the offset
                if (tripleCollision) {
                    tripleCollision = false;

                    params.width = Math.round(availableSpace / 3);
                    params.setMargins(2 * params.width, params.topMargin, params.rightMargin, params.bottomMargin);
                } else {
                    params.width = Math.round(availableSpace / 2);
                    params.setMargins(params.width, params.topMargin, params.rightMargin, params.bottomMargin);
                }
            } else if (!collisionPrev && collisionNext) {

                params.width = Math.round(availableSpace / 2);
                params.setMargins(0, params.topMargin, params.rightMargin, params.bottomMargin);

            } else if (collisionPrev ^ collisionPrevPrev) {
                //Set previous
                RelativeLayout prevEvent = eventList.get(currentEvent - 1);
                RelativeLayout.LayoutParams prevLp = (RelativeLayout.LayoutParams) prevEvent.getLayoutParams();
                prevLp.width = Math.round(availableSpace / 3);
                prevEvent.setLayoutParams(prevLp);

                //Set current
                params.width = Math.round(availableSpace / 3);
                params.setMargins(params.width, params.topMargin, params.rightMargin, params.bottomMargin);

                //Tell next one
                tripleCollision = true;
            }

            //Update params
            event.setLayoutParams(params);
            currentEvent++;

            //Add to main view and setup click to room finder listener
            mainScheduleLayout.addView(event);
            this.Listener(event);
        }
    }*/
/*
    private boolean overlap(long startTime1, long endTime1, long startTime2, long endTime2) {
        return !(endTime1 < startTime2 || startTime1 > endTime2);
    }*/

    /**
     * Setup an click listener which is connected to the room finder to locate rooms of the shown lectures
     *
     * @param v View to bind to
     */
    /*private void Listener(View v) {
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

                // TODO Open map directly
                // Launch the room finder activity
                Intent i = new Intent(activity, RoomFinderActivity.class);
                i.setAction(Intent.ACTION_SEARCH);
                i.putExtra(SearchManager.QUERY, strList[0]);
                activity.startActivity(i);
            }
        });
    }*/
}