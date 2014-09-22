package de.tum.in.tumcampus.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.models.LecturesSearchRow;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

/**
 * This class handles the view output of the results for finding lectures via
 * TUMOnline used in {@link de.tum.in.tumcampus.activities.LecturesPersonalActivity}
 * and {@link de.tum.in.tumcampus.activities.ChatRoomsSearchActivity}. It
 * implements {@link se.emilsjolander.stickylistheaders.StickyListHeadersAdapter} to
 * show semester info as sticky header.
 */

public class LecturesListAdapter extends BaseAdapter implements StickyListHeadersAdapter {

    private final ArrayList<String> filters;

    // the layout of the list
	static class ViewHolder {
		TextView tvDozent;
		TextView tvLectureName;
		TextView tvTypeSWSSemester;
	}

	// The list of lectures
	private static List<LecturesSearchRow> lecturesList;

	private final LayoutInflater mInflater;

	// constructor
	public LecturesListAdapter(Context context, List<LecturesSearchRow> results) {
		lecturesList = results;
		mInflater = LayoutInflater.from(context);

        filters = new ArrayList<String>();
        for (LecturesSearchRow result : results) {
            String item = result.getSemester_id();
            if (filters.indexOf(item) == -1) {
                filters.add(item);
            }
        }
	}

	@Override
	public int getCount() {
		return lecturesList.size();
	}

	@Override
	public Object getItem(int position) {
		return lecturesList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.activity_lectures_listview, parent, false);
			holder = new ViewHolder();

			// set UI elements
			holder.tvLectureName = (TextView) convertView
					.findViewById(R.id.tvLectureName);
			holder.tvTypeSWSSemester = (TextView) convertView
					.findViewById(R.id.tvTypeSWSSemester);
			holder.tvDozent = (TextView) convertView
					.findViewById(R.id.tvDozent);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		LecturesSearchRow lvItem = lecturesList.get(position);

		// if we have something to display - set for each lecture element
		if (lvItem != null) {
			holder.tvLectureName.setText(lvItem.getTitel());
			holder.tvTypeSWSSemester.setText(lvItem.getStp_lv_art_name()
					+ " - " + lvItem.getSemester_id() + " - "
					+ lvItem.getDauer_info() + " SWS");
			holder.tvDozent.setText(lvItem.getVortragende_mitwirkende());
		}

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
        //set header text as first char in name
        String headerText = lecturesList.get(pos).getSemester_name();
        holder.text.setText(headerText);
        return convertView;
    }

    @Override
    public long getHeaderId(int i) {
        return filters.indexOf(lecturesList.get(i).getSemester_id());
    }

    static class HeaderViewHolder {
        TextView text;
    }
}
