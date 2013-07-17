package de.tum.in.tumcampusapp.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.CalendarView;
import android.widget.TextView;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.ActivityForAccessingTumOnline;
import de.tum.in.tumcampusapp.auxiliary.Const;

public class TimetableActivity extends ActivityForAccessingTumOnline {
	CalendarView calendar;
	TextView timetable;

	public TimetableActivity() {
		super(Const.KALENDER, R.layout.activity_timetable);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		timetable = (TextView) findViewById(R.id.timetable);

		super.requestFetch();
	}

	@Override
	public void onFetch(String rawResponse) {
		// TODO Do something meaningful with the XML file containing all events
		timetable.setText(rawResponse);
		progressLayout.setVisibility(View.GONE);
	}

}
