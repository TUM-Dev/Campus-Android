package de.tum.in.tumcampus.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.RoomfinderActivity;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.managers.CalendarManager;

/**
 * Fragment for each calendar-page.
 */
public class CalendarSectionFragment extends Fragment {
    private Activity activity;

    private final CalendarManager calendarManager;
    private Date currentDate = new Date();
    private final ArrayList<RelativeLayout> eventList = new ArrayList<RelativeLayout>();
    private RelativeLayout eventView;
    private RelativeLayout mainScheduleLayout;

    public CalendarSectionFragment() {
        calendarManager = new CalendarManager(getActivity());
    }

    private RelativeLayout inflateEventView() {
        LayoutInflater layoutInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return (RelativeLayout) layoutInflater.inflate(R.layout.layout_time_entry, null);
    }

    private LayoutParams initLayoutParams(float hours) {

        int oneHourHeight = (int) (activity.getResources().getDimension(R.dimen.time_one_hour));
        int height = (int) (oneHourHeight * hours);

        return new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, height);
    }

    @SuppressWarnings("unchecked")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        String date = getArguments().getString("date");
        boolean updateMode = getArguments().getBoolean("update_mode");

        View rootView = inflater.inflate(R.layout.fragment_calendar_section, container, false);

        if (!updateMode) {
            final ScrollView scrollview = ((ScrollView) rootView.findViewById(R.id.scrollview));

            // Scroll to a default position
            scrollview.post(new Runnable() {
                @Override
                public void run() {
                    scrollview.scrollTo(0, (int) getResources().getDimension(R.dimen.default_scroll_position));
                }
            });

            activity = getActivity();
            currentDate = Utils.getDateTimeISO(date);
            mainScheduleLayout = (RelativeLayout) rootView.findViewById(R.id.main_schedule_layout);

            updateCalendarView();
            mainScheduleLayout.setClickable(true);
        }

        return rootView;
    }

    @SuppressWarnings("deprecation")
    private void parseEvents() {
        Date dateStart;
        Date dateEnd;
        float start;
        float end;
        float hours;

        // Cursor cursor = kalMgr.getFromDbForDate(currentDate);
        Cursor cursor = calendarManager.getAllFromDb();
        int id = 1;
        Integer previousId=null;
        while (cursor.moveToNext()) {

            final String status = cursor.getString(1);
            final String strStart = cursor.getString(5);
            final String strEnd = cursor.getString(6);

            int year = Integer.valueOf(currentDate.getYear()) + 1900;
            int month = Integer.valueOf(currentDate.getMonth()) + 1;
            int day = Integer.valueOf(currentDate.getDate());

            String requestedDateString = year + "-" + String.format("%02d", month) + "-" + String.format("%02d", day);

            if (strStart.contains(requestedDateString)
                    && !status.equals("CANCEL")) {
                eventView = inflateEventView();
                dateStart = Utils.getISODateTime(strStart);
                dateEnd = Utils.getISODateTime(strEnd);

                start = dateStart.getHours() * 60 + dateStart.getMinutes();
                end = dateEnd.getHours() * 60 + dateStart.getMinutes();

                hours = (end - start) / 60f;

                // Set params to eventLayout
                LayoutParams params = initLayoutParams(hours);
                setStartOfEntry(params, start / 60f);

                //Check if room is present and set text accordingly
                String room=cursor.getString(7);
                if(room!=null && room.length()!=0) {
                    setText(eventView, cursor.getString(3) + " / " + cursor.getString(7));
                    eventView.setTag(cursor.getString(7));
                }else{
                    setText(eventView, cursor.getString(3));
                }

                // Try to fix overlapping events
                Integer eventId=cursor.getInt(1);
                if(eventId!=null && eventId>0){
                    if(previousId!=null){
                        params.addRule(RelativeLayout.LEFT_OF, previousId);
                    }
                    previousId=eventId+previousId;
                    eventView.setId(eventId);
                }

                eventView.setLayoutParams(params);
                eventList.add(eventView);

                eventView = inflateEventView();
                dateStart = Utils.getISODateTime(strStart);
                dateEnd = Utils.getISODateTime(strEnd);

                start = dateStart.getHours() * 60 + dateStart.getMinutes()+30;
                end = dateEnd.getHours() * 60 + dateStart.getMinutes()+30;

                hours = (end - start) / 60f;

                // Set params to eventLayout
                 params = initLayoutParams(hours);
                setStartOfEntry(params, start / 60f);

                //Check if room is present and set text accordingly
                 room=cursor.getString(7);
                if(room!=null && room.length()!=0) {
                    setText(eventView, cursor.getString(3) + " / " + cursor.getString(7));
                    eventView.setTag(cursor.getString(7));
                }else{
                    setText(eventView, cursor.getString(3));
                }

                // Try to fix overlapping events
                eventId=cursor.getInt(1);
                if(eventId!=null && eventId>0){
                    if(previousId!=null){
                        params.addRule(RelativeLayout.LEFT_OF, previousId);
                    }
                    previousId=eventId+previousId;
                    eventView.setId(eventId);
                }

                eventView.setLayoutParams(params);
                eventList.add(eventView);
            }
        }
    }

    private void setStartOfEntry(LayoutParams params, float start) {
        int oneHourHeight = (int) activity.getResources().getDimension(
                R.dimen.time_one_hour);
        int marginTop = (int) (oneHourHeight * start);
        params.setMargins(0, marginTop, 0, 0);
    }

    private void setText(RelativeLayout entry, String text) {
        TextView textView = (TextView) entry.findViewById(R.id.entry_title);
        textView.setText(text);
    }

    /**
     * Binds the Calender to the room finder
     *
     * @param v
     */
    private void Listener(View v) {
        v.setClickable(true);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //try to get the location of the lectures: sometimes none is provided
                String room= (String) v.getTag();
                if (room == null) {
                    Log.e("TCA", "No tag found on view");
                    return;
                }
                final String strList[] = room.split(",");

                //Launch the roomfinder activity
                Intent i = new Intent(activity, RoomfinderActivity.class);
                i.putExtra("NAME", strList[0]);
                startActivity(i);
            }
        });

    }

    private void updateCalendarView() {
        eventList.clear();
        parseEvents();
        mainScheduleLayout.removeAllViews();
        Log.i("Total lectures found", String.valueOf(eventList.size()));
        for (RelativeLayout event : eventList) {
            mainScheduleLayout.addView(event);
            Listener(event);
        }
    }
}