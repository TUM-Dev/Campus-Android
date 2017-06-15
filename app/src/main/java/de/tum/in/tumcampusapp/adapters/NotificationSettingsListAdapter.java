package de.tum.in.tumcampusapp.adapters;
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
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import de.tum.in.tumcampusapp.R;

public class NotificationSettingsListAdapter extends BaseAdapter{
    private ArrayList<Pair<Integer, Integer>> dailySchedule;
    private LayoutInflater inflater;
    private Activity activity;
    static class ViewHolder {
        TextView weekday;
        Button time;
        CheckBox isActive;
    }

    public NotificationSettingsListAdapter(Activity activity, ArrayList<Pair<Integer,Integer>> dailySchedule){
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
        if (position < getCount()){
            return dailySchedule.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View view, ViewGroup viewGroup) {
        final ViewHolder viewHolder;
        if (view == null) {
            viewHolder = new ViewHolder();
            view = inflater.inflate(R.layout.notification_schedule_listitem, null);
            viewHolder.weekday = (TextView) view.findViewById(R.id.weekday);
            viewHolder.time = (Button) view.findViewById(R.id.notification_time);
            viewHolder.isActive = (CheckBox) view.findViewById(R.id.notification_active);
            view.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) view.getTag();
        }
        Calendar it = Calendar.getInstance();
        it.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        it.add(Calendar.DAY_OF_WEEK, position);
        SimpleDateFormat format = new SimpleDateFormat("EEEE");
        String dayOfWeekString = format.format(it.getTime());
        viewHolder.weekday.setText(dayOfWeekString);
        viewHolder.isActive.setOnClickListener(new AlarmActivatedListener(position));
        final Pair<Integer, Integer> hourMinute = dailySchedule.get(position);
        viewHolder.time.setOnClickListener(new OnTimeClickListener(activity,position, hourMinute.first, hourMinute.second));
        if (hourMinute.first != -1) {
            viewHolder.time.setText(getTimeString(hourMinute.first, hourMinute.second));
            viewHolder.isActive.setChecked(true);
        }else{
            viewHolder.time.setText(getTimeString(0,0));
            viewHolder.isActive.setChecked(false);
        }
        return view;
    }

    private String getTimeString(int hour, int minute){
        return (hour+":"+((minute < 10)?"0"+minute:minute));
    }

    private class OnTimeClickListener implements View.OnClickListener{
        private Context context;
        private final int position;
        private int defaultHour;
        private int defaultMinute;
        public OnTimeClickListener(Context context, int position, int hour, int minute){
            this.context = context;
            this.position = position;
            this.defaultHour = hour;
            this.defaultMinute = minute;
        }
        @Override
        public void onClick(View view) {
            showTimePicker();
        }
        private void showTimePicker(){
             new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                    if(hour < 6 || hour > 15) {
                        Toast.makeText(context,"Invalid hour, please choose an hour inbetween 06:00 and 15:00", Toast.LENGTH_SHORT).show();
                    }else{
                        dailySchedule.set(position, new Pair<>(hour, minute));
                        notifyDataSetChanged();
                    }
                }
            }, defaultHour, defaultMinute, true).show();
        }
    }

    private class AlarmActivatedListener implements View.OnClickListener{
        private final int position;
        public AlarmActivatedListener(int position){
            this.position = position;
        }

        @Override
        public void onClick(View checkBox) {
            final boolean isChecked = ((CheckBox)checkBox).isChecked();
            if (!isChecked){
                dailySchedule.set(position, new Pair<>(-1,-1));
            }
            else{
                dailySchedule.set(position, new Pair<>(9,30));
            }
            notifyDataSetChanged();
        }
    }
}
