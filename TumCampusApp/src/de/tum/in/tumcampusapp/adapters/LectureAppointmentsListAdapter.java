package de.tum.in.tumcampusapp.adapters;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.models.LectureAppointmentsRow;

/**
 * Generates the output of the ListView on the LectureAppointments activity.
 * 
 * used by: LectureAppointments
 * 
 * linked files: res.layout.termine_listview
 * 
 * @author Daniel G. Mayr
 * @review Thomas Behrens
 */

public class LectureAppointmentsListAdapter extends BaseAdapter {

	// the layout
	static class ViewHolder {
		TextView tvTerminBetreff;
		TextView tvTerminOrt;
		TextView tvTerminZeit;
	}

	// list of Appointments to one lecture
	private static List<LectureAppointmentsRow> terminList;

	private final LayoutInflater mInflater;

	public LectureAppointmentsListAdapter(Context context, List<LectureAppointmentsRow> results) {
		terminList = results;
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return terminList.size();
	}

	@Override
	public Object getItem(int position) {
		return terminList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@SuppressLint("SimpleDateFormat")
	@SuppressWarnings("deprecation")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.activity_lecturesappointments_listview, null);
			holder = new ViewHolder();

			// set UI elements
			holder.tvTerminZeit = (TextView) convertView.findViewById(R.id.tvTerminZeit);
			holder.tvTerminOrt = (TextView) convertView.findViewById(R.id.tvTerminOrt);
			holder.tvTerminBetreff = (TextView) convertView.findViewById(R.id.tvTerminBetreff);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		LectureAppointmentsRow lvItem = terminList.get(position);

		// only show if lecture has a title and enough info
		if (lvItem != null) {
			holder.tvTerminOrt.setText(lvItem.getOrt());
			String line2 = lvItem.getArt();
			// only show betreff if available
			if (lvItem.getTermin_betreff() != null) {
				line2 += " - " + lvItem.getTermin_betreff();
			}
			holder.tvTerminBetreff.setText(line2);

			// zeitdarstellung setzen
			// parse dates
			// this is the template for the date in the xml file
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			try {
				Date start = formatter.parse(lvItem.getBeginn_datum_zeitpunkt());
				Date ende = formatter.parse(lvItem.getEnde_datum_zeitpunkt());

				// make two calendar instances
				Calendar cnow = Calendar.getInstance();
				Calendar cstart = Calendar.getInstance();
				cstart.setTime(start);

				// date formats for the day output
				SimpleDateFormat endHoursOutput = new SimpleDateFormat("HH:mm");
				SimpleDateFormat DateOutput = new SimpleDateFormat("dd.MM.yyyy HH:mm");
				String output = "";

				// output if same day: we only show the date once
				if (start.getMonth() == ende.getMonth() && start.getDate() == ende.getDate()) {
					output = Utils.getWeekDayByDate(start) + " " + DateOutput.format(start) + " - " + endHoursOutput.format(ende);
				} else {
					// show it normally
					output = Utils.getWeekDayByDate(start) + " " + DateOutput.format(start) + " - " + DateOutput.format(ende);
				}

				// grey it, if in past
				if (cstart.before(cnow)) {
					output = "<font color=\"#444444\">" + output + "</font>";
				}

				holder.tvTerminZeit.setText(Html.fromHtml(output));

			} catch (Exception ex) {
				holder.tvTerminZeit.setText(lvItem.getBeginn_datum_zeitpunkt() + " - " + lvItem.getEnde_datum_zeitpunkt());
			}

		}
		return convertView;
	}
}
