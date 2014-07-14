package de.tum.in.tumcampus.adapters;

import java.util.List;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;
import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.models.ChatMember;
import de.tum.in.tumcampus.models.ChatMessage2;

public class ChatHistoryAdapter extends BaseAdapter {

	// Layout of the list row
	static class ViewHolder {
		TextView tvUser;
		TextView tvMessage;
		TextView tvTimestamp;
	}
	
	private List<ChatMessage2> messageHistory;
	
	private final LayoutInflater inflater;
	
	private ChatMember currentChatMember;
	
	public ChatHistoryAdapter(Context context, List<ChatMessage2> messageHistory, ChatMember currentChatMember) {
		this.messageHistory = messageHistory;
		inflater = LayoutInflater.from(context);
		this.currentChatMember = currentChatMember;
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
			convertView = inflater.inflate(R.layout.activity_chat_history_row, parent, false);
			holder = new ViewHolder();

			// set UI elements
			holder.tvUser = (TextView) convertView.findViewById(R.id.tvUser);
			holder.tvMessage = (TextView) convertView.findViewById(R.id.tvMessage);
			holder.tvTimestamp = (TextView) convertView.findViewById(R.id.tvTime);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		ChatMessage2 chatMessage = messageHistory.get(position);
		if (chatMessage != null) {
			holder.tvUser.setText(chatMessage.getMember().getDisplayName());
			holder.tvMessage.setText(chatMessage.getText());
			holder.tvTimestamp.setText(chatMessage.getTimestampString());
			
			LinearLayout chatMessageLayout = (LinearLayout) convertView.findViewById(R.id.chatMessageLayout);
			LayoutParams chatMessageLayoutParams = (LayoutParams) chatMessageLayout.getLayoutParams();
			
			FrameLayout chatFrameLayout = (FrameLayout) convertView.findViewById(R.id.chatFrameLayout);
			
			if ("bot".equals(chatMessage.getMember().getLrzId())) {
				chatMessageLayoutParams.gravity = Gravity.CENTER;
				chatFrameLayout.setPadding(100, 0, 100, 0); // Add left and right padding
				holder.tvUser.setText("");
				holder.tvTimestamp.setText("");
			} else if (currentChatMember.getUrl().equals(chatMessage.getMember().getUrl())) {
				chatMessageLayout.setBackgroundResource(R.drawable.bubble_right);
				chatMessageLayoutParams.gravity = Gravity.RIGHT;
				chatFrameLayout.setPadding(100, 0, 0, 0); // Add left padding
			} else {
				chatMessageLayout.setBackgroundResource(R.drawable.bubble_left);
				chatMessageLayoutParams.gravity = Gravity.LEFT;
				chatFrameLayout.setPadding(0, 0, 100, 0); // Add right padding
			}
			
			chatMessageLayout.setLayoutParams(chatMessageLayoutParams);
		}

		return convertView;
	}	
}
