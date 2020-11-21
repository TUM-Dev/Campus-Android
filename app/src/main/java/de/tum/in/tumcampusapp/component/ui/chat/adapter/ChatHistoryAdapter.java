package de.tum.in.tumcampusapp.component.ui.chat.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
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

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.ui.chat.model.ChatMember;
import de.tum.in.tumcampusapp.component.ui.chat.model.ChatMessage;

public class ChatHistoryAdapter extends BaseAdapter {

    private static final int OUTGOING_MESSAGE = 0;
    private static final int INCOMING_MESSAGE = 1;

    private List<ChatMessage> chatHistoryList = new ArrayList<>();

    private Context mContext;
    private OnRetrySendListener mRetryListener;

    private ChatMember currentChatMember;

    public ChatHistoryAdapter(Context context, ChatMember member) {
        mContext = context;
        mRetryListener = (OnRetrySendListener) mContext;
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
        return chatHistoryList.get(position).getId();
    }

    public boolean isEmpty() {
        return getCount() == 0;
    }

    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMember member = getItem(position).getMember();
        return currentChatMember.getId() == member.getId() ? OUTGOING_MESSAGE : INCOMING_MESSAGE;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        boolean isOutgoing = getItemViewType(position) == OUTGOING_MESSAGE;
        int layout = isOutgoing ? R.layout.activity_chat_history_row_outgoing
                                : R.layout.activity_chat_history_row_incoming;

        ChatMessage message = getItem(position);
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(layout, parent, false);
            holder = new ViewHolder(convertView, isOutgoing);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.bind(mContext, message, isOutgoing, mRetryListener);
        return convertView;
    }

    public void add(ChatMessage unsentMessage) {
        chatHistoryList.add(unsentMessage);
        notifyDataSetChanged();
    }

    public interface OnRetrySendListener {
        void onRetrySending(ChatMessage message);
    }

    // Layout of the list row
    private static class ViewHolder {

        LinearLayout containerLayout;
        TextView userTextView;
        TextView messageTextView;
        TextView timestampTextView;
        ProgressBar sendingProgressBar;
        ImageView statusImageView;

        public ViewHolder(View itemView, boolean isOutgoingMessage) {
            containerLayout = itemView.findViewById(R.id.chatMessageLayout);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            timestampTextView = itemView.findViewById(R.id.timeTextView);

            if (isOutgoingMessage) {
                sendingProgressBar = itemView.findViewById(R.id.progressBar);
                statusImageView = itemView.findViewById(R.id.statusImageView);
            } else {
                userTextView = itemView.findViewById(R.id.userTextView);
            }
        }

        public void bind(Context context, ChatMessage message,
                         boolean isOutgoingMessage, OnRetrySendListener retryListener) {
            messageTextView.setText(message.getText());

            if (isOutgoingMessage) {
                boolean isSending = message.getSendingStatus() == ChatMessage.STATUS_SENDING;
                statusImageView.setVisibility(isSending ? View.GONE : View.VISIBLE);
                sendingProgressBar.setVisibility(isSending ? View.VISIBLE : View.GONE);
                updateSendingStatus(context, message);
            } else {
                userTextView.setText(message.getMember().getDisplayName());
            }

            String lrzId = message.getMember().getLrzId();
            if (lrzId != null && lrzId.equals("bot")) {
                userTextView.setText("");
                timestampTextView.setText("");
            }

            containerLayout.setOnClickListener(view -> resendIfError(context, message, retryListener));
        }

        private void resendIfError(Context context,
                                   ChatMessage message, OnRetrySendListener retryListener) {
            if (message.getSendingStatus() == ChatMessage.STATUS_ERROR) {
                AlertDialog dialog = new AlertDialog.Builder(context)
                        .setMessage(R.string.chat_message_try_again)
                        .setPositiveButton(R.string.retry, (dialogInterface, i) -> {
                            retryListener.onRetrySending(message);
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .create();

                if (dialog.getWindow() != null) {
                    dialog.getWindow()
                            .setBackgroundDrawableResource(R.drawable.rounded_corners_background);
                }

                dialog.show();
            }
        }

        private void updateSendingStatus(Context context, ChatMessage message) {
            boolean inProgress = message.getSendingStatus() == ChatMessage.STATUS_SENDING;

            statusImageView.setVisibility(inProgress ? View.GONE : View.VISIBLE);
            sendingProgressBar.setVisibility(inProgress ? View.VISIBLE : View.GONE);

            int darkTextColor = ContextCompat.getColor(context, R.color.text_secondary);

            if (inProgress) {
                timestampTextView.setTextColor(darkTextColor);
                timestampTextView.setText(message.getFormattedTimestamp(context));
                return;
            }

            boolean isError = message.getSendingStatus() == ChatMessage.STATUS_ERROR;

            Drawable statusIcon;
            int iconTint;

            if (isError) {
                statusIcon = ContextCompat.getDrawable(context, R.drawable.ic_error_outline);
                iconTint = ContextCompat.getColor(context, R.color.error);
            } else {
                statusIcon = ContextCompat.getDrawable(context, R.drawable.ic_check);
                iconTint = ContextCompat.getColor(context, R.color.tum_blue);
            }

            if (isError) {
                timestampTextView.setText(R.string.chat_message_send_error);
                timestampTextView.setTextColor(iconTint);
            } else {
                int textColor = ContextCompat.getColor(context, R.color.text_secondary);
                timestampTextView.setTextColor(textColor);
                timestampTextView.setText(message.getFormattedTimestamp(context));
            }

            if (statusIcon != null) {
                statusIcon.setTint(iconTint);
                statusImageView.setImageDrawable(statusIcon);
            }
        }

    }

}
