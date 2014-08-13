package de.tum.in.tumcampus.adapters;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.LecturesSearchActivity;
import de.tum.in.tumcampus.models.LecturesSearchRow;

/**
 * This class handles the view output of the results for finding lectures via
 * TUMOnline
 * 
 * {@link LecturesSearchActivity} activity or the {@link MyLectures} activity
 * 
 * linked files: res.layout.lectures_listview
 * 
 * @author Daniel G. Mayr
 * @review Thomas Behrens
 */

public class LecturesSearchListAdapter extends BaseAdapter {

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
	public LecturesSearchListAdapter(Context context,
			List<LecturesSearchRow> results) {
		lecturesList = results;
		mInflater = LayoutInflater.from(context);
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
			convertView = mInflater.inflate(
					R.layout.activity_lectures_listview, null);
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
}
