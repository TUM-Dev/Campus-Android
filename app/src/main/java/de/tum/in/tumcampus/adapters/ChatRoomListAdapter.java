package de.tum.in.tumcampus.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.models.managers.ChatRoomManager;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

/**
 * This class handles the view output of the results for finding lectures via
 * TUMOnline used in {@link de.tum.in.tumcampus.activities.LecturesPersonalActivity}
 * and {@link de.tum.in.tumcampus.activities.ChatRoomsActivity}. It
 * implements {@link se.emilsjolander.stickylistheaders.StickyListHeadersAdapter} to
 * show semester info as sticky header.
 */

public class ChatRoomListAdapter extends CursorAdapter implements StickyListHeadersAdapter {

    // the layout of the list
    static class ViewHolder {
        TextView tvDozent;
        TextView tvLectureName;
    }

    private final ArrayList<String> filters;
    private final LayoutInflater mInflater;

    // constructor
    public ChatRoomListAdapter(Context context, Cursor results) {
        super(context, results, false);
        mInflater = LayoutInflater.from(context);

        filters = new ArrayList<String>();
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        View convertView = mInflater.inflate(R.layout.activity_lectures_listview, viewGroup, false);
        ViewHolder holder = new ViewHolder();

        // set UI elements
        holder.tvLectureName = (TextView) convertView.findViewById(R.id.tvLectureName);
        holder.tvDozent = (TextView) convertView.findViewById(R.id.tvDozent);
        convertView.findViewById(R.id.tvTypeSWSSemester).setVisibility(View.GONE);

        convertView.setTag(holder);
        return convertView;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();
        holder.tvLectureName.setText(cursor.getString(ChatRoomManager.COL_NAME));
        holder.tvDozent.setText(cursor.getString(ChatRoomManager.COL_CONTRIBUTOR));
    }

    // Generate header view
    @Override
    public View getHeaderView(int pos, View convertView, ViewGroup parent) {
        HeaderViewHolder holder;
        if (convertView == null) {
            holder = new HeaderViewHolder();
            convertView = mInflater.inflate(R.layout.header, parent, false);
            holder.text = (TextView) convertView.findViewById(R.id.lecture_header);
            convertView.setTag(holder);
        } else {
            holder = (HeaderViewHolder) convertView.getTag();
        }
        //set header text as first char in name
        Cursor item = (Cursor) getItem(pos);
        String semester = item.getString(ChatRoomManager.COL_SEMESTER);
        if(semester.isEmpty())
            semester = mContext.getString(R.string.my_chat_rooms);
        holder.text.setText(semester);
        return convertView;
    }

    @Override
    public long getHeaderId(int i) {
        Cursor item = (Cursor) getItem(i);
        String semester = item.getString(ChatRoomManager.COL_SEMESTER);
        if (!filters.contains(semester)) {
            filters.add(semester);
        }
        return filters.indexOf(semester);
    }

    static class HeaderViewHolder {
        TextView text;
    }
}
