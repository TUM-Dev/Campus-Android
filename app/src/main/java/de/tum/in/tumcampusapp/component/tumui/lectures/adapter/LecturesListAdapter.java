package de.tum.in.tumcampusapp.component.tumui.lectures.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.other.generic.adapter.SimpleStickyListHeadersAdapter;
import de.tum.in.tumcampusapp.component.tumui.lectures.activity.LecturesPersonalActivity;
import de.tum.in.tumcampusapp.component.tumui.lectures.model.Lecture;
import de.tum.in.tumcampusapp.component.ui.chat.activity.ChatRoomsActivity;

/**
 * This class handles the view output of the results for finding lectures via
 * TUMOnline used in {@link LecturesPersonalActivity}
 * and {@link ChatRoomsActivity}. It
 * implements {@link se.emilsjolander.stickylistheaders.StickyListHeadersAdapter} to
 * show semester info as sticky header.
 */

public class LecturesListAdapter extends SimpleStickyListHeadersAdapter<Lecture> {

    public LecturesListAdapter(Context context, List<Lecture> results) {
        super(context, results);
    }

    @Override
    public String genenrateHeaderName(Lecture item) {
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
            holder.tvLectureName = convertView.findViewById(R.id.lectureNameTextView);
            holder.tvTypeSWSSemester = convertView.findViewById(R.id.typeTextView);
            holder.tvDozent = convertView.findViewById(R.id.professorTextView);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Lecture lvItem = infoList.get(position);

        // if we have something to display - set for each lecture element
        if (lvItem != null) {
            holder.tvLectureName.setText(lvItem.getTitle());
            String details = context.getString(R.string.lecture_list_item_details_format_string,
                    lvItem.getLectureType(), lvItem.getSemesterId(), lvItem.getDuration());
            holder.tvTypeSWSSemester.setText(details);
            holder.tvDozent.setText(lvItem.getLecturers());
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
