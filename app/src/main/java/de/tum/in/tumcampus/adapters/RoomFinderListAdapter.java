package de.tum.in.tumcampus.adapters;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.tumonline.TUMRoomFinderRequest;

/**
 * Custom UI adapter for a list of employees.
 * 
 * @author Anas Chakfeh
 */
public class RoomFinderListAdapter extends BaseAdapter {
	static class ViewHolder {
		TextView tvDetails1;
		TextView tvDetails2;
		TextView tvName;
	}

    private ArrayList<HashMap<String, String>> data;
	private static LayoutInflater inflater = null;

	// public ImageLoader imageLoader;

	public RoomFinderListAdapter(Activity a,
			ArrayList<HashMap<String, String>> d) {
        Activity activity = a;
		data = d;
		inflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		// imageLoader=new ImageLoader(activity.getApplicationContext());
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
		View vi = convertView;
		if (convertView == null)
			vi = inflater.inflate(R.layout.list_roomfinder_item, null);

		TextView roomTitle = (TextView) vi.findViewById(R.id.title); // roomtitle
		TextView buildingTitle = (TextView) vi.findViewById(R.id.building); // building
		TextView campusTitle = (TextView) vi.findViewById(R.id.campus); // campus

		HashMap<String, String> room = new HashMap<String, String>();
		room = data.get(position);

		// Setting all values in listview
		roomTitle.setText(room.get(TUMRoomFinderRequest.KEY_Room
				+ TUMRoomFinderRequest.KEY_TITLE));
		buildingTitle.setText(room.get(TUMRoomFinderRequest.KEY_Building
				+ TUMRoomFinderRequest.KEY_TITLE));
		campusTitle.setText(room.get(TUMRoomFinderRequest.KEY_Campus
				+ TUMRoomFinderRequest.KEY_TITLE));
		return vi;
	}
}
