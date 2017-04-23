package de.tum.in.tumcampusapp.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.entities.ChatRoom;
import de.tum.in.tumcampusapp.managers.ChatRoomManager;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

/**
 * This class handles the view output of the results for finding lectures via
 * TUMOnline used in {@link de.tum.in.tumcampusapp.activities.LecturesPersonalActivity}
 * and {@link de.tum.in.tumcampusapp.activities.ChatRoomsActivity}. It
 * implements {@link StickyListHeadersAdapter} to
 * show semester info as sticky header.
 */

public class ChatRoomListAdapter extends ArrayAdapter<ChatRoom> implements StickyListHeadersAdapter {

    private final LayoutInflater mInflater;
    private final boolean showDateAndNumber;
    private final List<String> filters;

    private final List<ChatRoom> rooms;
    private final Context context;

    // the layout of the list
    static class ViewHolder {
        TextView tvDozent;
        TextView tvLectureName;
        TextView tvMembers;
        TextView tvLastmsg;
        LinearLayout llAdditionalInfo;

    }

    // constructor
    public ChatRoomListAdapter(Context context, List<ChatRoom> results, boolean joinedOnly) {
        super(context, 0, results);

        this.rooms = results;
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.filters = new ArrayList<>();
        this.showDateAndNumber = joinedOnly;
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup viewGroup) {
        ViewHolder holder;
        if(convertView == null) {
            convertView = mInflater.inflate(R.layout.activity_lectures_listview, viewGroup, false);
            holder = new ViewHolder();

            // set UI elements
            holder.tvLectureName = (TextView) convertView.findViewById(R.id.tvLectureName);
            holder.tvDozent = (TextView) convertView.findViewById(R.id.tvDozent);
            holder.tvMembers = (TextView) convertView.findViewById(R.id.tvMembers);
            holder.tvLastmsg = (TextView) convertView.findViewById(R.id.tvLastmsg);
            holder.llAdditionalInfo = (LinearLayout) convertView.findViewById(R.id.llAdditionalInfo);
            convertView.findViewById(R.id.tvTypeSWSSemester).setVisibility(View.GONE);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tvLectureName.setText(rooms.get(pos).getName());
        holder.tvDozent.setText(rooms.get(pos).getContributor());

        if (showDateAndNumber) {
            holder.tvMembers.setText(rooms.get(pos).getMembers());
            //holder.tvLastmsg.setText(DateUtils.getTimeOrDay(cursor.getString(8), context));
            holder.llAdditionalInfo.setVisibility(View.VISIBLE);
        } else {
            holder.tvDozent.setText(rooms.get(pos).getContributor());
        }




        return convertView;
    }

    // Generate header view
    @Override
    public View getHeaderView(int pos, View view, ViewGroup parent) {
        HeaderViewHolder holder;
        View convertView = view;
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
        if (semester.isEmpty()) {
            semester = context.getString(R.string.my_chat_rooms);
        }
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
