package de.tum.in.tumcampusapp.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.CalendarView;
import android.widget.CalendarView.OnDateChangeListener;
import android.widget.TextView;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.ActivityForAccessingTumOnline;
import de.tum.in.tumcampusapp.auxiliary.Const;

public class TimetableActivity extends ActivityForAccessingTumOnline implements
		OnDateChangeListener {
	CalendarView calendar;
	TextView timetable;

	public TimetableActivity() {
		super(Const.KALENDER, R.layout.activity_timetable);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		timetable = (TextView) findViewById(R.id.timetable);
		calendar = (CalendarView) findViewById(R.id.calendar);

		calendar.setOnDateChangeListener(this);

		super.requestFetch();
	}

	@Override
	public void onFetch(String rawResponse) {
		// TODO Dow something meaningful with the XML file containing all events
		progressLayout.setVisibility(View.GONE);
	}

	@Override
	public void onSelectedDayChange(CalendarView view, int year, int month,
			int dayOfMonth) {
	}
}
