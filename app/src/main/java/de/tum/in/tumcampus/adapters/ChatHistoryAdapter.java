package de.tum.in.tumcampus.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.models.ChatMember;
import de.tum.in.tumcampus.models.ListChatMessage;

public class ChatHistoryAdapter extends BaseAdapter {

    private final Context mContext;

    // Layout of the list row
	static class ViewHolder {
		TextView tvUser;
		TextView tvMessage;
		TextView tvTimestamp;
	}
	
	private final List<ListChatMessage> messageHistory;
	
	private final LayoutInflater inflater;
	
	private final ChatMember currentChatMember;
	
	public ChatHistoryAdapter(Context context, List<ListChatMessage> messageHistory, ChatMember currentChatMember) {
		this.messageHistory = messageHistory;
		inflater = LayoutInflater.from(context);
		this.currentChatMember = currentChatMember;
        mContext = context;
	}
	
	@Override
	public int getCount() {
		return messageHistory.size();
	}

	@Override
	public Object getItem(int position) {
		return messageHistory.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        ListChatMessage chatMessage = messageHistory.get(position);
        return currentChatMember.getUrl().equals(chatMessage.getMember().getUrl())?0:1;
    }

    @Override
	public View getView(int position, View convertView, ViewGroup parent) {		
		ViewHolder holder;
        ListChatMessage chatMessage = messageHistory.get(position);
        boolean outgoing = currentChatMember.getUrl().equals(chatMessage.getMember().getUrl());

		if (convertView == null) {
            int layout = outgoing?R.layout.activity_chat_history_row_outgoing:R.layout.activity_chat_history_row_incoming;
			convertView = inflater.inflate(layout, parent, false);
			holder = new ViewHolder();

			// set UI elements
			holder.tvUser = (TextView) convertView.findViewById(R.id.tvUser);
			holder.tvMessage = (TextView) convertView.findViewById(R.id.tvMessage);
			holder.tvTimestamp = (TextView) convertView.findViewById(R.id.tvTime);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

        holder.tvUser.setText(chatMessage.getMember().getDisplayName());
        holder.tvMessage.setText(chatMessage.getText());
        holder.tvTimestamp.setText(chatMessage.getTimestampString(mContext));

        if ("bot".equals(chatMessage.getMember().getLrzId())) {
            //noinspection deprecation
            holder.tvUser.setText("");
            holder.tvTimestamp.setText("");
        }

		return convertView;
	}

    public boolean add(int i, ListChatMessage downloadedMessage) {
        for(ListChatMessage message : messageHistory) {
            if(message.getTimestamp().equals(downloadedMessage.getTimestamp()) &&
                    message.getText().equals(downloadedMessage.getText()))
                return false;
        }
        messageHistory.add(i, downloadedMessage);
        return true;
    }

    public void add(ListChatMessage downloadedMessage) {
        for(ListChatMessage message : messageHistory) {
            if(message.getTimestamp().equals(downloadedMessage.getTimestamp()) &&
                    message.getText().equals(downloadedMessage.getText()))
                return;
        }
        messageHistory.add(downloadedMessage);
        notifyDataSetChanged();
    }
}
