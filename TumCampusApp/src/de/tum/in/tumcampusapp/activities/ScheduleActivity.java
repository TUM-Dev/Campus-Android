package de.tum.in.tumcampusapp.activities;

import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.ActivityForAccessingTumOnline;
import de.tum.in.tumcampusapp.auxiliary.Const;

public class ScheduleActivity extends ActivityForAccessingTumOnline {
	RelativeLayout mainScheduleLayout;

	public ScheduleActivity() {
		super(Const.FETCH_NOTHING, R.layout.activity_timetable_dayview);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mainScheduleLayout = (RelativeLayout) findViewById(R.id.main_schedule_layout);
		
		ArrayList<RelativeLayout> scheduleList = new ArrayList<RelativeLayout>();

		scheduleList.add(createEntry(1, 0.5f, "Erstes"));
		scheduleList.add(createEntry(2, 2, "Zweites"));
		scheduleList.add(createEntry(5.5f, 2, "Drittes"));
		scheduleList.add(createEntry(10, 4, "Viertes"));
		
		checkOverlappings(scheduleList);
		
		for (RelativeLayout entry : scheduleList) {
			mainScheduleLayout.addView(entry);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}
	
	private void checkOverlappings(ArrayList<RelativeLayout> scheduleList) {
		for (RelativeLayout entry : scheduleList) {
		}
	}

	private RelativeLayout createEntry(float start,float hours,  String text) {
		RelativeLayout entry = (RelativeLayout) inflateEntry();
		LayoutParams params = initLayoutParams(hours);
		setStartOfEntry(params, start);
		setText(entry, text);
		entry.setLayoutParams(params);

		return entry;
	}

	private RelativeLayout inflateEntry() {
		LayoutInflater layoutInflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		return (RelativeLayout) layoutInflater.inflate(
				R.layout.layout_time_entry, null);
	}

	private void setText(RelativeLayout entry, String text) {
		TextView textView = (TextView) entry.findViewById(R.id.entry_title);
		textView.setText(text);
	}

	private LayoutParams initLayoutParams(float hours) {
		int oneHourHeight = (int) getResources().getDimension(
				R.dimen.time_one_hour);
		int height = (int) (oneHourHeight * hours);
		return new LayoutParams(LayoutParams.MATCH_PARENT, height);
	}

	private void setStartOfEntry(LayoutParams params, float start) {
		int oneHourHeight = (int) getResources().getDimension(
				R.dimen.time_one_hour);
		int marginTop = (int) (oneHourHeight * start);
		params.setMargins(0, marginTop, 0, 0);
	}

	@Override
	public void onFetch(String rawResponse) {
	}

}
