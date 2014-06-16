package de.tum.in.tumcampus.adapters;

import java.util.List;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.models.ChatMessage;

public class ChatHistoryAdapter extends BaseAdapter {

	// Layout of the list row
	static class ViewHolder {
		TextView tvUser;
		TextView tvMessage;
		TextView tvTimestamp;
	}
	
	private List<ChatMessage> messageHistory;
	
	private final LayoutInflater inflater;
	
	public ChatHistoryAdapter(Context context, List<ChatMessage> messageHistory) {
		this.messageHistory = messageHistory;
		inflater = LayoutInflater.from(context);
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
	public View getView(int position, View convertView, ViewGroup parent) {		
		ViewHolder holder;

		if (convertView == null) {
			convertView = inflater.inflate(R.layout.activity_chat_history_row, null);
			holder = new ViewHolder();

			// set UI elements
			holder.tvUser = (TextView) convertView.findViewById(R.id.tvUser);
			holder.tvMessage = (TextView) convertView.findViewById(R.id.tvMessage);
			holder.tvTimestamp = (TextView) convertView.findViewById(R.id.tvTime);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		ChatMessage chatMessage = messageHistory.get(position);
		if (chatMessage != null) {
			holder.tvUser.setText(chatMessage.getMember());
			holder.tvMessage.setText(chatMessage.getText());
			//holder.tvTimestamp.setText(chatMessage.getTimestampString());
			
			LinearLayout chatMessageLayout = (LinearLayout) convertView.findViewById(R.id.chatMessageLayout);
			
			// TODO: Put user and timestamp in the bubble
			//LayoutParams layoutParams = (LayoutParams) convertView.findViewById(R.id.chatMessageLayout).getLayoutParams();
			
			if (position % 2 == 0) { // TODO: Check if it's current user's message
				chatMessageLayout.setBackgroundResource(R.drawable.bubble_right);
				//layoutParams.gravity = Gravity.RIGHT;
			} else {
				chatMessageLayout.setBackgroundResource(R.drawable.bubble_left);
				//layoutParams.gravity = Gravity.LEFT;
			}
			
			//holder.tvMessage.setLayoutParams(layoutParams);
		}

		return convertView;
	}	
}
