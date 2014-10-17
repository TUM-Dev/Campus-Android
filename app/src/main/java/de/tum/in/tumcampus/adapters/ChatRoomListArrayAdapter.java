package de.tum.in.tumcampus.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.models.ChatRoom;
import de.tum.in.tumcampus.models.Exam;
import de.tum.in.tumcampus.models.Room;
import de.tum.in.tumcampus.models.managers.ChatRoomManager;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

/**
 * Custom UI adapter for a list of rooms.
 */
public class ChatRoomListArrayAdapter extends BaseAdapter implements StickyListHeadersAdapter {

    static class ViewHolder {
        TextView tvDozent;
        TextView tvLectureName;
    }

	private static List<ChatRoom> rooms;

	private final Context context;

	private final LayoutInflater mInflater;

	public ChatRoomListArrayAdapter(Context context, List<ChatRoom> results) {
		this.rooms = results;
		this.mInflater = LayoutInflater.from(context);
		this.context = context;
	}

	@Override
	public int getCount() {
		return rooms.size();
	}

	@Override
	public Object getItem(int position) {
		return rooms.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup viewGroup) {
		ViewHolder holder;

		// find and init UI
		if (convertView == null) {
            convertView = mInflater.inflate(R.layout.activity_lectures_listview, viewGroup, false);
            holder = new ViewHolder();

            // set UI elements
            holder.tvLectureName = (TextView) convertView.findViewById(R.id.tvLectureName);
            holder.tvDozent = (TextView) convertView.findViewById(R.id.tvDozent);
            convertView.findViewById(R.id.tvTypeSWSSemester).setVisibility(View.GONE);

            convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		// fill UI with data
        ChatRoom room=rooms.get(position);

        holder.tvLectureName.setText(room.getName());
        holder.tvDozent.setText("");
		return convertView;
	}
    // Generate header view
    @Override
    public View getHeaderView(int pos, View convertView, ViewGroup parent) {
        HeaderViewHolder holder;
        if (convertView == null) {
            holder = new HeaderViewHolder();
            convertView = mInflater.inflate(R.layout.header, parent, false);
            holder.text = (TextView) convertView.findViewById(R.id.lecture_header);
            convertView.setTag(holder);
        } else {
            holder = (HeaderViewHolder) convertView.getTag();
        }

        holder.text.setText("Meine Räume");
        return convertView;
    }

    @Override
    public long getHeaderId(int i) {
        return 1;
    }

    static class HeaderViewHolder {
        TextView text;
    }
}
