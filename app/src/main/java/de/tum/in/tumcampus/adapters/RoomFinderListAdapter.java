package de.tum.in.tumcampus.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.tumonline.TUMRoomFinderRequest;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

/**
 * Custom UI adapter for a list of employees.
 */
public class RoomFinderListAdapter extends BaseAdapter implements StickyListHeadersAdapter {

    static class ViewHolder {
		TextView tvRoomTitle;
        TextView tvBuildingTitle;
	}

    private final ArrayList<HashMap<String, String>> data;
	private static LayoutInflater inflater;

	public RoomFinderListAdapter(Activity activity, ArrayList<HashMap<String, String>> d) {
		data = d;
		inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public Object getItem(int position) {
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_roomfinder_item, parent, false);

            holder = new ViewHolder();
            holder.tvRoomTitle = (TextView) convertView.findViewById(R.id.startup_actionbar_title);
            holder.tvBuildingTitle = (TextView) convertView.findViewById(R.id.building);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

		HashMap<String, String> room = data.get(position);

		// Setting all values in listView
		holder.tvRoomTitle.setText(room.get(TUMRoomFinderRequest.KEY_ROOM + TUMRoomFinderRequest.KEY_TITLE));
        holder.tvBuildingTitle.setText(room.get(TUMRoomFinderRequest.KEY_Building + TUMRoomFinderRequest.KEY_TITLE));
		return convertView;
	}

    // Generate header view
    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        HeaderViewHolder holder;
        if (convertView == null) {
            holder = new HeaderViewHolder();
            convertView = inflater.inflate(R.layout.header, parent, false);
            holder.text = (TextView) convertView.findViewById(R.id.lecture_header);
            convertView.setTag(holder);
        } else {
            holder = (HeaderViewHolder) convertView.getTag();
        }

        //set header text as first char in name
        HashMap<String, String> room = data.get(position);
        String headerText = room.get(TUMRoomFinderRequest.KEY_Campus + TUMRoomFinderRequest.KEY_TITLE);
        holder.text.setText(headerText);
        return convertView;
    }

    @Override
    public long getHeaderId(int i) {
        String headerText = data.get(i).get(TUMRoomFinderRequest.KEY_Campus + TUMRoomFinderRequest.KEY_TITLE);
        if(headerText.equals("Garching-Hochbrück"))
            return 'X';
        if(headerText.equals("Sonstiges"))
            return 'Z';
        return data.get(i).get(TUMRoomFinderRequest.KEY_Campus + TUMRoomFinderRequest.KEY_ID).charAt(0);
    }

    static class HeaderViewHolder {
        TextView text;
    }
}
