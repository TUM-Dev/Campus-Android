package de.tum.in.tumcampusapp.component.ui.chat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.tumui.lectures.activity.LecturesPersonalActivity;
import de.tum.in.tumcampusapp.component.ui.chat.activity.ChatRoomsActivity;
import de.tum.in.tumcampusapp.component.ui.chat.model.ChatRoomAndLastMessage;
import de.tum.in.tumcampusapp.utils.DateUtils;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

/**
 * This class handles the view output of the results for finding lectures via
 * TUMOnline used in {@link LecturesPersonalActivity}
 * and {@link ChatRoomsActivity}. It
 * implements {@link StickyListHeadersAdapter} to
 * show semester info as sticky header.
 */

public class ChatRoomListAdapter extends BaseAdapter implements StickyListHeadersAdapter {

    private List<ChatRoomAndLastMessage> rooms;
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
    public ChatRoomListAdapter(Context context, List<ChatRoomAndLastMessage> results, int mode) {
        this.mInflater = LayoutInflater.from(context);
        this.filters = new ArrayList<>();
        this.showDateAndNumber = mode == 1;
        this.rooms = results;
        this.mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItem = convertView;
        ViewHolder holder;
        ChatRoomAndLastMessage room = getItem(position);

        if (listItem == null) {
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
        holder.tvLectureName.setText(room.getChatRoomDbRow()
                                         .getName());
        holder.tvDozent.setText(room.getText());

        if (showDateAndNumber) {
            holder.tvMembers.setText(String.format(Locale.getDefault(), "%d", room.getChatRoomDbRow()
                                                                                  .getMembers()));
            holder.tvLastmsg.setText(DateUtils.getTimeOrDayISO(room.getTimestamp(), mContext));
            holder.llAdditionalInfo.setVisibility(View.VISIBLE);
        } else {
            holder.tvDozent.setText(room.getChatRoomDbRow()
                                        .getContributor());
        }

        return listItem;
    }

    @Override
    public View getHeaderView(int pos, View view, ViewGroup parent) {
        HeaderViewHolder holder;
        View convertview = view;
        if (convertview == null) {
            holder = new HeaderViewHolder();
            convertview = mInflater.inflate(R.layout.header, parent, false);
            holder.text = convertview.findViewById(R.id.lecture_header);
            convertview.setTag(holder);
        } else {
            holder = (HeaderViewHolder) convertview.getTag();
        }
        //set header text as first char in name
        ChatRoomAndLastMessage item = (ChatRoomAndLastMessage) getItem(pos);
        String semester = item.getChatRoomDbRow()
                              .getSemester();
        if (semester.isEmpty()) {
            semester = mContext.getString(R.string.my_chat_rooms);
        }
        holder.text.setText(semester);
        return convertview;
    }

    @Override
    public long getHeaderId(int i) {
        ChatRoomAndLastMessage item = (ChatRoomAndLastMessage) getItem(i);
        String semester = item.getChatRoomDbRow()
                              .getSemester();
        if (!filters.contains(semester)) {
            filters.add(semester);
        }
        return filters.indexOf(semester);
    }

    @Override
    public ChatRoomAndLastMessage getItem(int position) {
        if (rooms != null) {
            return rooms.get(position);
        } else {
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        if (rooms != null) {
            return rooms.get(position)
                        .getChatRoomDbRow()
                        .getId();
        } else {
            return -1;
        }
    }

    static class HeaderViewHolder {
        TextView text;
    }

    @Override
    public int getCount() {
        return rooms.size();
    }

    public void updateRooms(List<ChatRoomAndLastMessage> newRoomlist) {
        rooms = newRoomlist;
        notifyDataSetChanged();
    }
}
