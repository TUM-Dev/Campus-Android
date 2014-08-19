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
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
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
    private RelativeLayout mainScheduleLayout;

    public CalendarSectionFragment() {
        calendarManager = new CalendarManager(getActivity());
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


            mainScheduleLayout.setClickable(true);
            mainScheduleLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

                @Override
                public void onGlobalLayout() {
                    // Ensure you call it only once :
                    mainScheduleLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                    // Here you can get the size :)
                    updateCalendarView();
                }
            });
        }

        return rootView;
    }

    @SuppressWarnings("deprecation")
    private void parseEvents() {
        Date dateStart;
        Date dateEnd;
        float start;
        float end;
        float duration;

        // Cursor cursor = kalMgr.getFromDbForDate(currentDate);
        Cursor cursor = calendarManager.getAllFromDb();
        int id = 1;
        Integer previousId = null;

        LayoutInflater layoutInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout eventView;
        int oneHourHeight = ((int) activity.getResources().getDimension(R.dimen.time_one_hour));

        while (cursor.moveToNext()) {
            //Fetch some data
            final String status = cursor.getString(1);
            final String strStart = cursor.getString(5);
            final String strEnd = cursor.getString(6);

            //Format today date to database format
            SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
            String requestedDateString = sf.format(currentDate);

            //Check if event is today
            if (strStart.contains(requestedDateString) && !status.equals("CANCEL")) {
                //Create our layout_time_entry
                eventView = (RelativeLayout) layoutInflater.inflate(R.layout.layout_time_entry, null);

                dateStart = Utils.getISODateTime(strStart);
                dateEnd = Utils.getISODateTime(strEnd);

                start = dateStart.getHours() * 60 + dateStart.getMinutes();
                end = dateEnd.getHours() * 60 + dateStart.getMinutes();

                duration = (end - start) / 60f;

                // Height is determined by duration
                LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, Math.round(duration * oneHourHeight));

                // Offset by margin until start time is reached
                params.setMargins(0, Math.round(oneHourHeight * (start / 60f)), 0, 0);

                //Check if room is present and set text accordingly
                String room = cursor.getString(7);
                if (room != null && room.length() != 0) {
                    setText(eventView, cursor.getString(3) + " / " + cursor.getString(7));
                    eventView.setTag(cursor.getString(7));
                } else {
                    setText(eventView, cursor.getString(3));
                }

                // Try to fix overlapping events
                // TODO not yet working
                Integer eventId = cursor.getInt(1);
                if (eventId != null && eventId >= 0) {
                    if (previousId != null) {
                        params.addRule(RelativeLayout.LEFT_OF, previousId);
                        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    }
                    previousId = eventId;
                    eventView.setId(eventId);
                }
                eventView.setLayoutParams(params);
                eventList.add(eventView);

                //Create our layout_time_entry
                eventView = (RelativeLayout) layoutInflater.inflate(R.layout.layout_time_entry, null);

                dateStart = Utils.getISODateTime(strStart);
                dateEnd = Utils.getISODateTime(strEnd);

                start = dateStart.getHours() * 60 + dateStart.getMinutes();
                end = dateEnd.getHours() * 60 + dateStart.getMinutes();

                duration = (end - start) / 60f;

                // Height is determined by duration
                params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, Math.round(duration * oneHourHeight));

                // Offset by margin until start time is reached
                params.setMargins(0, Math.round(oneHourHeight * (start / 60f)), 0, 0);

                //Check if room is present and set text accordingly
                room = cursor.getString(7);
                if (room != null && room.length() != 0) {
                    setText(eventView, cursor.getString(3) + " / " + cursor.getString(7));
                    eventView.setTag(cursor.getString(7));
                } else {
                    setText(eventView, cursor.getString(3));
                }

                // Try to fix overlapping events
                // TODO not yet working
                eventId = cursor.getInt(1) + 1;
                if (eventId != null && eventId >= 0) {
                    if (previousId != null) {
                        params.addRule(RelativeLayout.LEFT_OF, previousId);
                        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    }
                    previousId = eventId;
                    eventView.setId(eventId);
                }
                eventView.setLayoutParams(params);
                eventList.add(eventView);
            }
        }
    }

    private void setText(RelativeLayout entry, String text) {
        TextView textView = (TextView) entry.findViewById(R.id.entry_title);
        textView.setText(text);
    }

    /**
     * Setup an click listner which is connected to the roomfinder to locate rooms of the shown lectures
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

        //Calc width so events don't overlap
        float avaibleSpace = mainScheduleLayout.getWidth() - getResources().getDimension(R.dimen.default_scroll_position);
        int width = Math.round((float) avaibleSpace / (float) eventList.size());
        Log.e("TCA", " width: " + width + " " + mainScheduleLayout.getWidth());

        //Add all events to the main view
        for (RelativeLayout event : eventList) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) event.getLayoutParams();
            params.width = width;
            event.setLayoutParams(params);

            mainScheduleLayout.addView(event);
            Listener(event);
        }
    }
}