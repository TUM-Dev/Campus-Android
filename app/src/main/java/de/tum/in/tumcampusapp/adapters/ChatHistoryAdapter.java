package de.tum.in.tumcampusapp.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.auxiliary.DateUtils;
import de.tum.in.tumcampusapp.managers.ChatMessageManager;
import de.tum.in.tumcampusapp.models.tumcabe.ChatMember;
import de.tum.in.tumcampusapp.models.tumcabe.ChatMessage;

public class ChatHistoryAdapter extends CursorAdapter {

    private final Context mContext;
    private final LayoutInflater inflater;
    private final ChatMember currentChatMember;
    private List<ChatMessage> unsentMessages = new ArrayList<>();
    public ChatMessage mCheckedItem;
    public ChatMessage mEditedItem;

    public int getSentCount() {
        return super.getCount();
    }

    public void removeUnsent(ChatMessage msg) {
        unsentMessages.remove(msg);
    }

    // Layout of the list row
    static class ViewHolder {
        TextView tvUser;
        TextView tvMessage;
        TextView tvTimestamp;
        ProgressBar pbSending;
        ImageView ivSent;
        public LinearLayout layout;
    }

    public ChatHistoryAdapter(Context context, Cursor messageHistory, ChatMember member) {
        super(context, messageHistory, false);
        inflater = LayoutInflater.from(context);
        currentChatMember = member;
        mContext = context;
    }

    @Override
    public int getCount() {
        return super.getCount() + unsentMessages.size();
    }

    @Override
    public Object getItem(int position) {
        int count = super.getCount();
        if (position < count) {
            Cursor cursor = getCursor();
            cursor.moveToPosition(position);
            return ChatMessageManager.toObject(cursor);
        } else {
            return unsentMessages.get(position - count);
        }
    }

    @Override
    public long getItemId(int position) {
        int count = super.getCount();
        if (position < count) {
            mCursor.moveToPosition(position);
            return mCursor.getLong(ChatMessageManager.COL_ID);
        } else {
            return 0;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (position > super.getCount()) {
            return 0;
        }
        ChatMessage msg = (ChatMessage) getItem(position);
        return currentChatMember.getId() == msg.getMember()
                                               .getId() ? 0 : 1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        int count = super.getCount();
        if (position < count) {
            return super.getView(position, convertView, viewGroup);
        }

        ChatMessage chatMessage = unsentMessages.get(position - count);
        View v = newView(mContext, null, viewGroup);
        bindViewChatMessage(v, chatMessage);
        return v;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        ViewHolder holder;
        boolean outgoing = true;
        if (cursor != null) {
            ChatMessage msg = ChatMessageManager.toObject(cursor);
            outgoing = currentChatMember.getId() == msg.getMember()
                                                       .getId();
        }

        int layout = outgoing ? R.layout.activity_chat_history_row_outgoing : R.layout.activity_chat_history_row_incoming;
        View view = inflater.inflate(layout, viewGroup, false);
        holder = new ViewHolder();

        // set UI elements
        holder.layout = view.findViewById(R.id.chatMessageLayout);

        holder.tvMessage = view.findViewById(R.id.tvMessage);
        holder.tvTimestamp = view.findViewById(R.id.tvTime);
        if (outgoing) {
            holder.pbSending = view.findViewById(R.id.progressBar);
            holder.ivSent = view.findViewById(R.id.sentImage);
        } else {
            //We only got the user on receiving things
            holder.tvUser = view.findViewById(R.id.tvUser);
        }

        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ChatMessage chatMessage = ChatMessageManager.toObject(cursor);
        bindViewChatMessage(view, chatMessage);
    }

    private void bindViewChatMessage(View view, ChatMessage chatMessage) {
        ViewHolder holder = (ViewHolder) view.getTag();

        holder.tvMessage.setText(chatMessage.getText());
        holder.tvTimestamp.setText(DateUtils.getTimeOrDayISO(chatMessage.getTimestamp(), mContext));

        if (holder.ivSent == null) {
            holder.tvUser.setText(chatMessage.getMember()
                                             .getDisplayName());
        } else {// Set status for outgoing messages (ivSent is not null)
            boolean sending = chatMessage.getStatus() == ChatMessage.STATUS_SENDING;
            holder.ivSent.setVisibility(sending ? View.GONE : View.VISIBLE);
            holder.pbSending.setVisibility(sending ? View.VISIBLE : View.GONE);
        }

        if (chatMessage.getMember()
                       .getLrzId()
                       .equals("bot")) {
            holder.tvUser.setText("");
            holder.tvTimestamp.setText("");
        }

        if ((mCheckedItem != null
             && mCheckedItem.getId() == chatMessage.getId()
             && (mCheckedItem.getStatus() == chatMessage.getStatus()))
            || (mEditedItem != null
                && mEditedItem.getId() == chatMessage.getId()
                && mEditedItem.getStatus() == chatMessage.getStatus())) {
            holder.layout.setBackgroundResource(R.drawable.bg_message_outgoing_selected);
        } else if (holder.ivSent != null) {
            holder.layout.setBackgroundResource(R.drawable.bg_message_outgoing);
        }
    }

    public void add(ChatMessage unsentMessage) {
        unsentMessages.add(unsentMessage);
        notifyDataSetChanged();
    }

    public void setUnsentMessages(List<ChatMessage> unsent) {
        unsentMessages = unsent;
    }
}
