package de.tum.in.tumcampus.adapters;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.models.LectureAppointmentsRow;

/**
 * Generates the output of the ListView on the {@link de.tum.in.tumcampus.activities.LecturesAppointmentsActivity} activity.
 */
public class LectureAppointmentsListAdapter extends BaseAdapter {

    // list of Appointments to one lecture
    private static List<LectureAppointmentsRow> appointmentList;
    private final LayoutInflater mInflater;

    public LectureAppointmentsListAdapter(Context context, List<LectureAppointmentsRow> results) {
        appointmentList = results;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return appointmentList.size();
    }

    @Override
    public Object getItem(int position) {
        return appointmentList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.activity_lecturesappointments_listview, parent, false);

            // save UI elements in view holder
            holder = new ViewHolder();
            holder.tvTerminZeit = (TextView) convertView.findViewById(R.id.tvTerminZeit);
            holder.tvTerminOrt = (TextView) convertView.findViewById(R.id.tvTerminOrt);
            holder.tvTerminBetreff = (TextView) convertView.findViewById(R.id.tvTerminBetreff);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        LectureAppointmentsRow lvItem = appointmentList.get(position);

        // only show if lecture has a title and enough info
        if (lvItem != null) {
            holder.tvTerminOrt.setText(lvItem.getOrt());
            String line2 = lvItem.getArt();
            // only show betreff if available
            if (lvItem.getTermin_betreff() != null) {
                line2 += " - " + lvItem.getTermin_betreff();
            }
            holder.tvTerminBetreff.setText(line2);

            // zeitdarstellung setzen
            // parse dates
            // this is the template for the date in the xml file
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
            try {

                Calendar start = Calendar.getInstance();
                Calendar ende = Calendar.getInstance();
                start.setTime(formatter.parse(lvItem.getBeginn_datum_zeitpunkt()));
                ende.setTime(formatter.parse(lvItem.getEnde_datum_zeitpunkt()));

                // make two calendar instances
                Calendar cnow = Calendar.getInstance();
                Calendar cstart = Calendar.getInstance();
                cstart.setTime(start.getTime());

                // date formats for the day output
                DateFormat endHoursOutput = SimpleDateFormat.getTimeInstance();
                DateFormat StartDateOutput = SimpleDateFormat.getDateTimeInstance();
                DateFormat EndDateOutput = SimpleDateFormat.getDateTimeInstance();

                // output if same day: we only show the date once
                String output;
                if (start.get(Calendar.MONTH) == ende.get(Calendar.MONTH) &&
                        start.get(Calendar.DATE) == ende.get(Calendar.DATE)) {
                    output = StartDateOutput.format(start.getTime()) + " - " + endHoursOutput.format(ende.getTime());
                } else {
                    // show it normally
                    output = StartDateOutput.format(start.getTime()) + " - " + EndDateOutput.format(ende.getTime());
                }

                // grey it, if in past
                if (cstart.before(cnow)) {
                    output = "<font color=\"#444444\">" + output + "</font>";
                }

                holder.tvTerminZeit.setText(Html.fromHtml(output));

            } catch (ParseException e) {
                holder.tvTerminZeit.setText(String.format("%s - %s",
                        lvItem.getBeginn_datum_zeitpunkt(), lvItem.getEnde_datum_zeitpunkt()));
            }

        }
        return convertView;
    }

    // the layout
    static class ViewHolder {
        TextView tvTerminBetreff;
        TextView tvTerminOrt;
        TextView tvTerminZeit;
    }
}
