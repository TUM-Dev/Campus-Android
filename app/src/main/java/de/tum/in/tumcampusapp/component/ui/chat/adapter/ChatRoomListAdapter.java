package de.tum.in.tumcampusapp.component.ui.chat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.tumui.lectures.activity.LecturesPersonalActivity;
import de.tum.in.tumcampusapp.component.ui.chat.activity.ChatRoomsActivity;
import de.tum.in.tumcampusapp.component.ui.chat.model.ChatRoomAndLastMessage;
import de.tum.in.tumcampusapp.component.ui.chat.model.ChatRoomDbRow;
import de.tum.in.tumcampusapp.utils.DateTimeUtils;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

/**
 * This class handles the view output of the results for finding lectures via
 * TUMOnline used in {@link LecturesPersonalActivity}
 * and {@link ChatRoomsActivity}. It
 * implements {@link StickyListHeadersAdapter} to
 * show semester info as sticky header.
 */

public class ChatRoomListAdapter extends BaseAdapter implements StickyListHeadersAdapter {

    private final boolean showDateAndNumber;
    private final List<String> filters;
    private List<ChatRoomAndLastMessage> rooms;
    private Context mContext;

    // constructor
    public ChatRoomListAdapter(Context context, List<ChatRoomAndLastMessage> results, int mode) {
        this.filters = new ArrayList<>();
        this.showDateAndNumber = mode == 1;
        this.rooms = results;
        this.mContext = context;
    }

    @Override
    public View getHeaderView(int pos, View convertView, ViewGroup parent) {
        HeaderViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext())
                                        .inflate(R.layout.header, parent, false);
            holder = new HeaderViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (HeaderViewHolder) convertView.getTag();
        }
        //set header text as first char in name
        ChatRoomAndLastMessage item = getItem(pos);
        String semester = item.getChatRoomDbRow()
                              .getSemester();

        if (semester.isEmpty()) {
            semester = mContext.getString(R.string.my_chat_rooms);
        }
        holder.textView.setText(semester);
        return convertView;
    }

    @Override
    public long getHeaderId(int i) {
        ChatRoomAndLastMessage item = getItem(i);
        String semester = item.getChatRoomDbRow()
                              .getSemester();
        if (!filters.contains(semester)) {
            filters.add(semester);
        }
        return filters.indexOf(semester);
    }

    @Override
    public int getCount() {
        return rooms.size();
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
                        .getLvId();
        } else {
            return -1;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        ChatRoomAndLastMessage room = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext())
                                        .inflate(R.layout.activity_lectures_listview, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ChatRoomDbRow chatRoom = room.getChatRoomDbRow();
        if (chatRoom == null) {
            return convertView;
        }

        holder.lectureNameTextView.setText(chatRoom.getName());
        if (room.getText() != null) {
            holder.professorTextView.setVisibility(View.VISIBLE);
            holder.professorTextView.setText(room.getText());
        } else {
            holder.professorTextView.setVisibility(View.GONE);
        }

        if (holder.unreadMessagesTextView != null && room.hasUnread()) {
            holder.unreadMessagesTextView.setVisibility(View.VISIBLE);
            holder.unreadMessagesTextView.setText(room.getNrUnread() >= 25 ? "25+" : room.getNrUnread() + "");
        } else if (holder.unreadMessagesTextView != null) {
            holder.unreadMessagesTextView.setVisibility(View.GONE);
        }

        if (showDateAndNumber) {
            holder.additionalInfoLayout.setVisibility(View.VISIBLE);

            String membersText = Integer.toString(chatRoom.getMembers());
            if (!membersText.isEmpty()) {
                holder.membersTextView.setVisibility(View.VISIBLE);
                holder.membersTextView.setText(membersText);
            } else {
                holder.membersTextView.setVisibility(View.GONE);
            }

            DateTime timestamp = room.getTimestamp();
            String lastMessageText = "";

            if (timestamp != null) {
                // TODO(pfent): Switch to DateTime in ChatRoomAndLastMessage
                lastMessageText = DateTimeUtils.INSTANCE.formatTimeOrDay(timestamp, mContext);
            }

            if (!lastMessageText.isEmpty()) {
                holder.lastMessageTextView.setVisibility(View.VISIBLE);
                holder.lastMessageTextView.setText(lastMessageText);
            } else {
                holder.lastMessageTextView.setVisibility(View.GONE);
            }
        } else {
            holder.additionalInfoLayout.setVisibility(View.GONE);

            String contributor = chatRoom.getContributor();
            if (!contributor.isEmpty()) {
                holder.professorTextView.setVisibility(View.VISIBLE);
                holder.professorTextView.setText(contributor);
            } else {
                holder.professorTextView.setVisibility(View.GONE);
            }
        }

        return convertView;
    }

    public void updateRooms(List<ChatRoomAndLastMessage> newRoomlist) {
        rooms = newRoomlist;
        notifyDataSetChanged();
    }

    static class ViewHolder {

        TextView lectureNameTextView;
        TextView membersTextView;
        TextView lastMessageTextView;
        TextView professorTextView;
        LinearLayout additionalInfoLayout;
        TextView unreadMessagesTextView;

        public ViewHolder(View itemView) {
            lectureNameTextView = itemView.findViewById(R.id.lectureNameTextView);
            professorTextView = itemView.findViewById(R.id.professorTextView);
            membersTextView = itemView.findViewById(R.id.membersTextView);
            lastMessageTextView = itemView.findViewById(R.id.lastMessageTextView);
            additionalInfoLayout = itemView.findViewById(R.id.additionalInfoLayout);
            unreadMessagesTextView = itemView.findViewById(R.id.unreadMessagesTextView);
        }

    }

    static class HeaderViewHolder {

        TextView textView;

        HeaderViewHolder(View itemView) {
            textView = itemView.findViewById(R.id.lecture_header);
        }

    }

}
