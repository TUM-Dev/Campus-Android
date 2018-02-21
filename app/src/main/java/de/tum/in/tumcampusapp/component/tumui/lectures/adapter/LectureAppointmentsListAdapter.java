package de.tum.in.tumcampusapp.component.tumui.lectures.adapter;

import android.content.Context;
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

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.tumui.lectures.activity.LecturesAppointmentsActivity;
import de.tum.in.tumcampusapp.component.tumui.lectures.model.LectureAppointmentsRow;
import de.tum.in.tumcampusapp.utils.Utils;

/**
 * Generates the output of the ListView on the {@link LecturesAppointmentsActivity} activity.
 */
public class LectureAppointmentsListAdapter extends BaseAdapter {

    // list of Appointments to one lecture
    private final List<LectureAppointmentsRow> appointmentList;
    private final LayoutInflater mInflater;
    // date formats for the day output
    private final DateFormat endHoursOutput = DateFormat.getTimeInstance();
    private final DateFormat startDateOutput = DateFormat.getDateTimeInstance();
    private final DateFormat endDateOutput = DateFormat.getDateTimeInstance();
    // parse dates
    // this is the template for the date in the xml file
    private final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);

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
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;
        View convertView = view;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.activity_lecturesappointments_listview, parent, false);

            // save UI elements in view holder
            holder = new ViewHolder();
            holder.tvTerminZeit = convertView.findViewById(R.id.tvTerminZeit);
            holder.tvTerminOrt = convertView.findViewById(R.id.tvTerminOrt);
            holder.tvTerminBetreff = convertView.findViewById(R.id.tvTerminBetreff);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        LectureAppointmentsRow lvItem = appointmentList.get(position);

        // only show if lecture has a title and enough info
        if (lvItem != null) {
            holder.tvTerminOrt.setText(lvItem.getOrt());
            StringBuilder line2 = new StringBuilder(lvItem.getArt());
            // only show betreff if available
            if (lvItem.getTermin_betreff() != null) {
                line2.append(" - ")
                     .append(lvItem.getTermin_betreff());
            }
            holder.tvTerminBetreff.setText(line2.toString());

            Calendar start = Calendar.getInstance();
            Calendar ende = Calendar.getInstance();
            try {
                start.setTime(formatter.parse(lvItem.getBeginn_datum_zeitpunkt()));
                ende.setTime(formatter.parse(lvItem.getEnde_datum_zeitpunkt()));

                // make two calendar instances
                Calendar cnow = Calendar.getInstance();
                Calendar cstart = Calendar.getInstance();
                cstart.setTime(start.getTime());

                // output if same day: we only show the date once
                StringBuilder output = new StringBuilder();
                if (start.get(Calendar.MONTH) == ende.get(Calendar.MONTH) &&
                    start.get(Calendar.DATE) == ende.get(Calendar.DATE)) {
                    output.append(startDateOutput.format(start.getTime()))
                          .append(" - ")
                          .append(endHoursOutput.format(ende.getTime()));
                } else {
                    // show it normally
                    output.append(startDateOutput.format(start.getTime()))
                          .append(" - ")
                          .append(endDateOutput.format(ende.getTime()));
                }

                // grey it, if in past
                if (cstart.before(cnow)) {
                    output.insert(0, "<font color=\"#444444\">");
                    output.append("</font>");
                }

                holder.tvTerminZeit.setText(Utils.fromHtml(output.toString()));

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
