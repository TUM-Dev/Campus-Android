package de.tum.in.tumcampus.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.models.ChatMember;
import de.tum.in.tumcampus.models.ListChatMessage;
import de.tum.in.tumcampus.models.managers.ChatMessageManager;

public class ChatHistoryAdapter extends CursorAdapter {

    private final Context mContext;
    private ArrayList<ListChatMessage> unsentMessages;

    // Layout of the list row
	static class ViewHolder {
        TextView tvUser;
        TextView tvMessage;
        TextView tvTimestamp;
        public ProgressBar pbSending;
        public ImageView ivSent;
    }
	
	private final LayoutInflater inflater;
	
	private final ChatMember currentChatMember;
	
	public ChatHistoryAdapter(Context context, Cursor messageHistory, ChatMember member) {
        super(context, messageHistory, true);
		inflater = LayoutInflater.from(context);
		currentChatMember = member;
        mContext = context;
	}
	
	@Override
	public int getCount() {
		return super.getCount()+unsentMessages.size();
	}

	@Override
	public Object getItem(int position) {
        int count = super.getCount();
        if(position<count) {
            Cursor cursor = getCursor();
            cursor.moveToPosition(position);
            return ChatMessageManager.toObject(cursor);
        } else {
            return unsentMessages.get(position-count);
        }
	}

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if(position>super.getCount())
            return 0;
        ListChatMessage msg = (ListChatMessage) getItem(position);
        return currentChatMember.getUrl().equals(msg.getMember().getUrl())?0:1;
    }

    @Override
	public View getView(int position, View convertView, ViewGroup viewGroup) {
        if(position<super.getCount())
            return super.getView(position, convertView, viewGroup);

        //TODO
		return newView(mContext, null, viewGroup);
	}

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        ViewHolder holder;
        boolean outgoing = true;
        if(cursor!=null) {
            ListChatMessage msg = ChatMessageManager.toObject(cursor);
            outgoing = currentChatMember.getUrl().equals(msg.getMember().getUrl());
        }

        int layout = outgoing?R.layout.activity_chat_history_row_outgoing:R.layout.activity_chat_history_row_incoming;
        View view = inflater.inflate(layout, viewGroup, false);
        holder = new ViewHolder();

        // set UI elements
        holder.tvUser = (TextView) view.findViewById(R.id.tvUser);
        holder.tvMessage = (TextView) view.findViewById(R.id.tvMessage);
        holder.tvTimestamp = (TextView) view.findViewById(R.id.tvTime);
        if(outgoing) {
            holder.pbSending = (ProgressBar) view.findViewById(R.id.progressBar);
            holder.ivSent = (ImageView) view.findViewById(R.id.sentImage);
        }

        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();

        ListChatMessage chatMessage = ChatMessageManager.toObject(cursor);

        holder.tvUser.setText(chatMessage.getMember().getDisplayName());
        holder.tvMessage.setText(chatMessage.getText());
        holder.tvTimestamp.setText(chatMessage.getTimestampString(mContext));
        holder.ivSent.setVisibility(View.VISIBLE);
        holder.pbSending.setVisibility(View.GONE);

        if (chatMessage.getMember().getLrzId().equals("bot")) {
            //noinspection deprecation
            holder.tvUser.setText("");
            holder.tvTimestamp.setText("");
        }
    }

    public void add(ListChatMessage unsentMessage) {
        unsentMessages.add(unsentMessage);
        notifyDataSetChanged();
    }

    public void sent(ListChatMessage sentMessage) {
        unsentMessages.remove(sentMessage);
        notifyDataSetChanged();
    }
}
