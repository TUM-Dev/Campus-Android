package de.tum.in.tumcampusapp.adapters;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.models.tumcabe.RoomFinderRoom;

/**
 * Custom UI adapter for a list of employees.
 */
public class RoomFinderListAdapter extends SimpleStickyListHeadersAdapter<RoomFinderRoom> {

    static class ViewHolder {
        TextView tvRoomTitle;
        TextView tvBuildingTitle;
    }

    public RoomFinderListAdapter(Activity activity, List<RoomFinderRoom> d) {
        super(activity, d);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;
        View convertView = view;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_roomfinder_item, parent, false);

            holder = new ViewHolder();
            holder.tvRoomTitle = convertView.findViewById(R.id.startup_actionbar_title);
            holder.tvBuildingTitle = convertView.findViewById(R.id.building);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        RoomFinderRoom room = infoList.get(position);

        // Setting all values in listView
        holder.tvRoomTitle.setText(room.getInfo());
        holder.tvBuildingTitle.setText(room.getAddress());
        return convertView;
    }
}
