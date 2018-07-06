package de.tum.in.tumcampusapp.component.other.generic.adapter;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Context;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.TextView;
import android.widget.Toast;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Locale;

import de.tum.in.tumcampusapp.R;

public class NotificationSettingsListAdapter extends BaseAdapter {
    private final ArrayList<Pair<Integer, Integer>> dailySchedule;
    private final LayoutInflater inflater;
    private final Activity activity;

    //Earliest hour a user can pick in the time picker dialog
    private final int EARLIEST_HOUR = 6;
    //Earliest minute a user can pick in the time picker dialog
    private final int LATEST_HOUR = 23;

    static class ViewHolder {
        TextView weekday;
        Button time;
        CheckBox isActive;
    }

    public NotificationSettingsListAdapter(Activity activity, ArrayList<Pair<Integer, Integer>> dailySchedule) {
        this.activity = activity;
        this.dailySchedule = dailySchedule;
        inflater = activity.getLayoutInflater();
    }

    @Override
    public int getCount() {
        return dailySchedule.size();
    }

    @Override
    public Object getItem(int position) {
        if (position < getCount()) {
            return dailySchedule.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Takes care of generating the notification settingsPrefix list.
     * For each day (Monday-Friday) add a list-item and display
     * in a checkbox whether the alarm is disabled for that day (hour == -1)
     * or enabled (hour != 1)
     */
    @Override
    public View getView(final int position, View view, ViewGroup viewGroup) {
        final ViewHolder viewHolder;
        if (view == null) {
            viewHolder = new ViewHolder();
            view = inflater.inflate(R.layout.notification_schedule_listitem, null);
            viewHolder.weekday = view.findViewById(R.id.weekday);
            viewHolder.time = view.findViewById(R.id.notification_time);
            viewHolder.isActive = view.findViewById(R.id.notification_active);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        DateTimeFormatter format = DateTimeFormat.forPattern("EEEE")
                .withLocale(Locale.getDefault());
        DateTime it = DateTime.now()
                .withDayOfWeek(DateTimeConstants.MONDAY)
                .plusDays(position);
        String dayOfWeekString = format.print(it);
        viewHolder.weekday.setText(dayOfWeekString);
        viewHolder.isActive.setOnClickListener(new AlarmActivatedListener(position));
        final Pair<Integer, Integer> hourMinute = dailySchedule.get(position);
        viewHolder.time.setOnClickListener(new OnTimeClickListener(activity, position, hourMinute.first, hourMinute.second));
        if (hourMinute.first != -1) {
            viewHolder.time.setText(getTimeString(hourMinute.first, hourMinute.second));
            viewHolder.isActive.setChecked(true);
        } else {
            viewHolder.time.setText(getTimeString(0, 0));
            viewHolder.isActive.setChecked(false);
        }
        return view;
    }

    /**
     * Returns a String in the format [one digit hour]:[zero prepended two digit minute]
     */
    private String getTimeString(int hour, int minute) {
        return (hour + ":" + ((minute < 10) ? "0" + minute : minute));
    }

    /**
     * Triggered when clicking the TextView, displaying the preferred notification time
     * It opens a time-picker dialog and sets the time to "defaultHour":"defaultMinute"
     */
    private class OnTimeClickListener implements View.OnClickListener {
        private final Context context;
        private final int position;
        private int defaultHour;
        private int defaultMinute;

        OnTimeClickListener(Context context, int position, int hour, int minute) {
            this.context = context;
            this.position = position;
            if (hour == -1 || minute == -1) {
                this.defaultHour = 9;
                this.defaultMinute = 30;
            }
            this.defaultHour = hour;
            this.defaultMinute = minute;
        }

        @Override
        public void onClick(View view) {
            showTimePicker();
        }

        private void showTimePicker() {
            new TimePickerDialog(context, (timePicker, hour, minute) -> {
                if (hour < EARLIEST_HOUR || hour > LATEST_HOUR) {
                    Toast.makeText(context, context.getString(R.string.invalid_notification_time)
                                            + " " + EARLIEST_HOUR + " - " + LATEST_HOUR, Toast.LENGTH_SHORT)
                         .show();
                } else {
                    dailySchedule.set(position, new Pair<>(hour, minute));
                    notifyDataSetChanged();
                }
            }, defaultHour, defaultMinute, true).show();
        }
    }

    /**
     * Enables / disables the alarm by setting the Hour-Minute pair of a weekday to:
     * -1,-1 (disabled)
     * else: (enabled)
     */

    private class AlarmActivatedListener implements View.OnClickListener {
        private final int position;

        AlarmActivatedListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View checkBox) {
            final boolean isChecked = ((Checkable) checkBox).isChecked();
            if (!isChecked) {
                dailySchedule.set(position, new Pair<>(-1, -1));
            } else {
                dailySchedule.set(position, new Pair<>(9, 30));
            }
            notifyDataSetChanged();
        }
    }
}
