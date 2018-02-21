package de.tum.in.tumcampusapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.auxiliary.DateUtils;
import de.tum.in.tumcampusapp.models.tumcabe.ChatMember;
import de.tum.in.tumcampusapp.models.tumcabe.ChatMessage;

public class ChatHistoryAdapter extends BaseAdapter {
    private static final Integer MSG_OUTGOING = 0;
    private static final Integer MSG_INCOMING = 1;

    private List<ChatMessage> chatHistoryList = new ArrayList<>();

    private ChatMember currentChatMember;
    private Context mContext;
    public ChatMessage mCheckedItem;
    public ChatMessage mEditedItem;

    // Layout of the list row
    static class ViewHolder {
        TextView tvUser;
        TextView tvMessage;
        TextView tvTimestamp;
        ProgressBar pbSending;
        ImageView ivSent;
        public LinearLayout layout;
    }

    public ChatHistoryAdapter(Context context, List<ChatMessage> chatHistory, ChatMember member) {
        mContext = context;
        chatHistoryList = chatHistory;
        currentChatMember = member;
    }

    public void updateHistory(List<ChatMessage> newHistory) {
        chatHistoryList = newHistory;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return chatHistoryList.size();
    }

    @Override
    public ChatMessage getItem(int position) {
        return chatHistoryList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return chatHistoryList.get(position)
                              .getId();
    }

    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (position > getCount()) {
            return 0;
        }

        ChatMember member = getItem(position).getMember();
        return currentChatMember.getId() == member.getId() ? MSG_OUTGOING : MSG_INCOMING;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        boolean outgoing = getItemViewType(position) == MSG_OUTGOING;
        int layout = outgoing ? R.layout.activity_chat_history_row_outgoing : R.layout.activity_chat_history_row_incoming;

        ChatMessage msg = getItem(position);
        ViewHolder holder;
        View listItem = convertView;

        if (listItem == null) {
            listItem = LayoutInflater.from(mContext)
                                     .inflate(layout, parent, false);
            holder = new ViewHolder();
            //Set UI elements
            holder.layout = listItem.findViewById(R.id.chatMessageLayout);

            holder.tvMessage = listItem.findViewById(R.id.tvMessage);
            holder.tvTimestamp = listItem.findViewById(R.id.tvTime);
            if (outgoing) {
                holder.pbSending = listItem.findViewById(R.id.progressBar);
                holder.ivSent = listItem.findViewById(R.id.sentImage);
            } else {
                //We only got the user on receiving things
                holder.tvUser = listItem.findViewById(R.id.tvUser);
            }
            listItem.setTag(holder);
        } else {
            holder = (ViewHolder) listItem.getTag();
        }

        holder.tvMessage.setText(msg.getText());
        holder.tvTimestamp.setText(DateUtils.getTimeOrDayISO(msg.getTimestamp(), mContext));

        if (!outgoing) {
            holder.tvUser.setText(msg.getMember()
                                     .getDisplayName());
        } else {// Set status for outgoing messages (ivSent is not null)
            boolean sending = msg.getSendingStatus() == ChatMessage.STATUS_SENDING;
            holder.ivSent.setVisibility(sending ? View.GONE : View.VISIBLE);
            holder.pbSending.setVisibility(sending ? View.VISIBLE : View.GONE);
        }

        if (msg.getMember()
               .getLrzId()
               .equals("bot")) {
            holder.tvUser.setText("");
            holder.tvTimestamp.setText("");
        }

        if ((mCheckedItem != null
             && mCheckedItem.getId() == msg.getId()
             && (mCheckedItem.getSendingStatus() == msg.getSendingStatus()))
            || (mEditedItem != null
                && mEditedItem.getId() == msg.getId()
                && mEditedItem.getSendingStatus() == msg.getSendingStatus())) {
            holder.layout.setBackgroundResource(R.drawable.bg_message_outgoing_selected);
        } else if (outgoing) {
            holder.layout.setBackgroundResource(R.drawable.bg_message_outgoing);
        }

        return listItem;
    }

    public void add(ChatMessage unsentMessage) {
        chatHistoryList.add(unsentMessage);
        notifyDataSetChanged();
    }
}
