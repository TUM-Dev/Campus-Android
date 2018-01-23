package de.tum.in.tumcampusapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.auxiliary.DateUtils;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.models.chatRoom.ChatRoomDbRow;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

/**
 * This class handles the view output of the results for finding lectures via
 * TUMOnline used in {@link de.tum.in.tumcampusapp.activities.LecturesPersonalActivity}
 * and {@link de.tum.in.tumcampusapp.activities.ChatRoomsActivity}. It
 * implements {@link StickyListHeadersAdapter} to
 * show semester info as sticky header.
 */

public class ChatRoomListAdapter extends BaseAdapter implements StickyListHeadersAdapter {

    private List<ChatRoomDbRow> rooms;
    private final LayoutInflater mInflater;
    private final boolean showDateAndNumber;
    private final List<String> filters;
    private Context mContext;

    // the layout of the list
    static class ViewHolder {
        TextView tvDozent;
        TextView tvLectureName;
        TextView tvMembers;
        TextView tvLastmsg;
        LinearLayout llAdditionalInfo;

    }

    // constructor
    public ChatRoomListAdapter(Context context, List<ChatRoomDbRow> results, int mode) {
        this.mInflater = LayoutInflater.from(context);
        this.filters = new ArrayList<>();
        this.showDateAndNumber = mode == 1;
        this.rooms = results;
        this.mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)   {
        View listItem = convertView;
        ViewHolder holder;
        ChatRoomDbRow room = getItem(position);

        if (listItem == null)   {
            listItem = mInflater.inflate(R.layout.activity_lectures_listview, parent, false);
            holder = new ViewHolder();
            //set UI elements
            holder.tvLectureName = listItem.findViewById(R.id.tvLectureName);
            holder.tvDozent = listItem.findViewById(R.id.tvDozent);
            holder.tvMembers = listItem.findViewById(R.id.tvMembers);
            holder.tvLastmsg = listItem.findViewById(R.id.tvLastmsg);
            holder.llAdditionalInfo = listItem.findViewById(R.id.llAdditionalInfo);
            listItem.findViewById(R.id.tvTypeSWSSemester)
                       .setVisibility(View.GONE);

            listItem.setTag(holder);
        } else {
            holder = (ViewHolder) listItem.getTag();
        }
        holder.tvLectureName.setText(room.getName());
        holder.tvDozent.setText(room.getText());

        if(showDateAndNumber)   {
            holder.tvMembers.setText(Integer.toString(room.getMembers()));
            holder.tvLastmsg.setText(DateUtils.getTimeOrDayISO(room.getTimestamp(), mContext));
            holder.llAdditionalInfo.setVisibility(View.VISIBLE);
        } else {
            holder.tvDozent.setText(room.getContributor());
        }
        Utils.logv("members " + room.getMembers() + ' ' + room.getTimestamp());

        return listItem;
    }

    @Override
    public View getHeaderView(int pos, View view, ViewGroup parent) {
        HeaderViewHolder holder;
        View convertview = view;
        if (convertview == null)    {
            holder = new HeaderViewHolder();
            convertview = mInflater.inflate(R.layout.header, parent, false);
            holder.text = convertview.findViewById(R.id.lecture_header);
            convertview.setTag(holder);
        } else {
            holder = (HeaderViewHolder) convertview.getTag();
        }
        //set header text as first char in name
        ChatRoomDbRow item = (ChatRoomDbRow) getItem(pos);
        String semester = item.getSemester();
        if (semester.isEmpty()) {
            semester = mContext.getString(R.string.my_chat_rooms);
        }
        holder.text.setText(semester);
        return convertview;
    }

    @Override
    public long getHeaderId(int i)  {
        ChatRoomDbRow item = (ChatRoomDbRow) getItem(i);
        String semester = item.getSemester();
        if (!filters.contains(semester))    {
            filters.add(semester);
        }
        return filters.indexOf(semester);
    }

    @Override
    public ChatRoomDbRow getItem(int position) {
        if (rooms != null) {
            return rooms.get(position);
        } else {
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        if (rooms != null)  {
            return rooms.get(position).getId();
        } else {
            return -1;
        }
    }

    static class HeaderViewHolder {
        TextView text;
    }

    @Override
    public int getCount()   {
        return rooms.size();
    }

    public void updateRooms(List<ChatRoomDbRow> newRoomlist) {
        rooms = newRoomlist;
        notifyDataSetChanged();
    }
}
