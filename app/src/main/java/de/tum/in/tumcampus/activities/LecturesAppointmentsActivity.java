package de.tum.in.tumcampus.activities;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.generic.ActivityForAccessingTumOnline;
import de.tum.in.tumcampus.adapters.LectureAppointmentsListAdapter;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.models.LectureAppointmentsRowSet;
import de.tum.in.tumcampus.tumonline.TUMOnlineConst;

/**
 * This activity provides the appointment dates to a given lecture using the
 * TUMOnline web service.
 * 
 * HINT: a valid TUM Online token is needed
 * 
 * NEEDS: stp_sp_nr and title set in incoming bundle (lecture id, title)
 */
public class LecturesAppointmentsActivity extends ActivityForAccessingTumOnline<LectureAppointmentsRowSet> {

	/** UI elements */
	private ListView lvTermine;

    public LecturesAppointmentsActivity() {
		super(TUMOnlineConst.LECTURES_APPOINTMENTS, LectureAppointmentsRowSet.class, R.layout.activity_lecturesappointments);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// set UI Elements
		lvTermine = (ListView) findViewById(R.id.lvTerminList);
        TextView tvTermineLectureName = (TextView) findViewById(R.id.tvTermineLectureName);

		Bundle bundle = this.getIntent().getExtras();
		// set Lecture Name (depends on bundle data)
		tvTermineLectureName.setText(bundle.getString(Const.TITLE_EXTRA).toUpperCase());
		requestHandler.setParameter("pLVNr", bundle.getString("stp_sp_nr"));

		super.requestFetch();

	}

	/** process data got from TUMOnline request and show the list view */
	@Override
	public void onFetch(LectureAppointmentsRowSet lecturesList) {
		// may happen if there are no appointments for the lecture
		if (lecturesList.getLehrveranstaltungenTermine() == null) {
            showError(R.string.no_appointments);
			return;
		}

		// set data to the ListView object nothing to click (yet)
		lvTermine.setAdapter(new LectureAppointmentsListAdapter(this, lecturesList.getLehrveranstaltungenTermine()));
        showLoadingEnded();
	}
}
