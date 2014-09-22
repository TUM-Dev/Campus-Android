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

/**
 * Custom UI adapter for a list of employees.
 */
public class RoomFinderListAdapter extends BaseAdapter {
	static class ViewHolder {
		TextView tvRoomTitle;
        TextView tvBuildingTitle;
        TextView tvCampusTitle;
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
            holder.tvCampusTitle = (TextView) convertView.findViewById(R.id.campus);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

		HashMap<String, String> room = data.get(position);

		// Setting all values in listview
		holder.tvRoomTitle.setText(room.get(TUMRoomFinderRequest.KEY_ROOM + TUMRoomFinderRequest.KEY_TITLE));
        holder.tvBuildingTitle.setText(room.get(TUMRoomFinderRequest.KEY_Building + TUMRoomFinderRequest.KEY_TITLE));
        holder.tvCampusTitle.setText(room.get(TUMRoomFinderRequest.KEY_Campus + TUMRoomFinderRequest.KEY_TITLE));
		return convertView;
	}
}
