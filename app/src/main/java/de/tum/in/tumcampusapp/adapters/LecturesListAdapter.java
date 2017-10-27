package de.tum.in.tumcampusapp.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.models.tumo.LecturesSearchRow;

/**
 * This class handles the view output of the results for finding lectures via
 * TUMOnline used in {@link de.tum.in.tumcampusapp.activities.LecturesPersonalActivity}
 * and {@link de.tum.in.tumcampusapp.activities.ChatRoomsActivity}. It
 * implements {@link se.emilsjolander.stickylistheaders.StickyListHeadersAdapter} to
 * show semester info as sticky header.
 */

public class LecturesListAdapter extends SimpleStickyListHeadersAdapter<LecturesSearchRow> {

    public static LecturesListAdapter newInstance(Context context, List<LecturesSearchRow> results) {
        return new LecturesListAdapter(context, results);
    }

    private LecturesListAdapter(Context context, List<LecturesSearchRow> results) {
        super(context, results);
    }

    @Override
    String genenrateHeaderName(LecturesSearchRow item) {
        String headerText = super.genenrateHeaderName(item);
        headerText = headerText.replaceAll("Sommersemester", this.context.getString(R.string.semester_summer));
        headerText = headerText.replaceAll("Wintersemester", this.context.getString(R.string.semester_winter));
        return headerText;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;
        View convertView = view;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.activity_lectures_listview, parent, false);
            holder = new ViewHolder();

            // set UI elements
            holder.tvLectureName = convertView.findViewById(R.id.tvLectureName);
            holder.tvTypeSWSSemester = convertView.findViewById(R.id.tvTypeSWSSemester);
            holder.tvDozent = convertView.findViewById(R.id.tvDozent);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        LecturesSearchRow lvItem = infoList.get(position);

        // if we have something to display - set for each lecture element
        if (lvItem != null) {
            holder.tvLectureName.setText(lvItem.getTitel());
            holder.tvTypeSWSSemester.setText(String.format("%s - %s - %s SWS", lvItem.getStp_lv_art_name(), lvItem.getSemester_id(), lvItem.getDauer_info()));
            holder.tvDozent.setText(lvItem.getVortragende_mitwirkende());
        }

        return convertView;
    }

    // the layout of the list
    static class ViewHolder {
        TextView tvDozent;
        TextView tvLectureName;
        TextView tvTypeSWSSemester;
    }
}
