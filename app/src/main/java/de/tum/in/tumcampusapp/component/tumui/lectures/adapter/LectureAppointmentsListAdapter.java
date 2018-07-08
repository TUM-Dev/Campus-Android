package de.tum.in.tumcampusapp.component.tumui.lectures.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.List;
import java.util.Locale;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.tumui.lectures.activity.LecturesAppointmentsActivity;
import de.tum.in.tumcampusapp.component.tumui.lectures.model.LectureAppointmentsRow;
import de.tum.in.tumcampusapp.utils.DateTimeUtils;
import de.tum.in.tumcampusapp.utils.Utils;

/**
 * Generates the output of the ListView on the {@link LecturesAppointmentsActivity} activity.
 */
public class LectureAppointmentsListAdapter extends BaseAdapter {

    // list of Appointments to one lecture
    private final List<LectureAppointmentsRow> appointmentList;
    private final LayoutInflater mInflater;
    // date formats for the day output
    private final DateTimeFormatter endHoursOutput = DateTimeFormat.mediumTime();
    private final DateTimeFormatter startDateOutput = DateTimeFormat.mediumDateTime();
    private final DateTimeFormatter endDateOutput = DateTimeFormat.mediumDateTime();
    // parse dates
    // this is the template for the date in the xml file
    private final DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")
                                                              .withLocale(Locale.US);

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
            if (!lvItem.getTermin_betreff()
                       .isEmpty()) {
                line2.append(" - ")
                     .append(lvItem.getTermin_betreff());
            }
            holder.tvTerminBetreff.setText(line2.toString());

            try {
                DateTime start = formatter.parseDateTime(lvItem.getBeginn_datum_zeitpunkt());
                DateTime end = formatter.parseDateTime(lvItem.getEnde_datum_zeitpunkt());

                // output if same day: we only show the date once
                StringBuilder output = new StringBuilder();
                if (DateTimeUtils.INSTANCE.isSameDay(start, end)) {
                    output.append(startDateOutput.print(start))
                          .append(" - ")
                          .append(endHoursOutput.print(end));
                } else {
                    // show it normally
                    output.append(startDateOutput.print(start))
                          .append(" - ")
                          .append(endDateOutput.print(end));
                }

                // grey it, if in past
                if (start.isBeforeNow()) {
                    output.insert(0, "<font color=\"#444444\">");
                    output.append("</font>");
                }

                holder.tvTerminZeit.setText(Utils.fromHtml(output.toString()));

            } catch (IllegalArgumentException e) {
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
