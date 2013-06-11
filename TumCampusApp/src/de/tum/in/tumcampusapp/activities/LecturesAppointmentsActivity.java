package de.tum.in.tumcampusapp.activities;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.ActivityForAccessingTumOnline;
import de.tum.in.tumcampusapp.adapters.LectureAppointmentsListAdapter;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.PersonalLayoutManager;
import de.tum.in.tumcampusapp.models.LectureAppointmentsRowSet;

/**
 * This activity provides the appointment dates to a given lecture using the
 * TUMOnline web service.
 * 
 * HINT: a valid TUM Online token is needed
 * 
 * NEEDS: stp_sp_nr and title set in incoming bundle (lecture id, title)
 * 
 * needed/linked files: res.layout.lecture_appointments, LectureAppointments
 * 
 * 
 * @solves [M5] Abhaltungstermine zu Lehrveranstaltungen einsehen
 * @author Daniel G. Mayr
 * @review Thomas Behrens // i found nothing tbd.
 */
@SuppressLint("DefaultLocale")
public class LecturesAppointmentsActivity extends ActivityForAccessingTumOnline {

	/** UI elements */
	private ListView lvTermine;
	private TextView tvTermineLectureName;

	public LecturesAppointmentsActivity() {
		super(Const.LECTURES_APPOINTMENTS, R.layout.activity_lecturesappointments);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// set UI Elements
		lvTermine = (ListView) findViewById(R.id.lvTerminList);
		tvTermineLectureName = (TextView) findViewById(R.id.tvTermineLectureName);

		Bundle bundle = this.getIntent().getExtras();
		// set Lecture Name (depends on bundle data)
		tvTermineLectureName.setText(bundle.getString(Const.TITLE_EXTRA).toUpperCase());
		requestHandler.setParameter("pLVNr", bundle.getString("stp_sp_nr"));

		super.requestFetch();

	}

	/** process data got from TUMOnline request and show the listview */
	@Override
	public void onFetch(String rawResponse) {
		Log.d(getClass().getSimpleName(), rawResponse);

		// deserialize xml
		Serializer serializer = new Persister();
		LectureAppointmentsRowSet lecturesList = null;
		try {
			lecturesList = serializer.read(LectureAppointmentsRowSet.class, rawResponse);
		} catch (Exception e) {
			Log.d("SIMPLEXML", "wont work: " + e.getMessage());
			errorLayout.setVisibility(View.VISIBLE);
			progressLayout.setVisibility(View.GONE);
			e.printStackTrace();
		}

		// may happen if there are no appointments for the lecture
		if (lecturesList == null || lecturesList.getLehrveranstaltungenTermine() == null) {
			errorLayout.setVisibility(View.VISIBLE);
			progressLayout.setVisibility(View.GONE);
			Toast.makeText(this, R.string.no_appointments, Toast.LENGTH_SHORT).show();
			return;
		}

		// set data to the ListView object
		// nothing to click (yet)
		lvTermine.setAdapter(new LectureAppointmentsListAdapter(this, lecturesList.getLehrveranstaltungenTermine()));
		progressLayout.setVisibility(View.GONE);
	}

	@Override
	protected void onResume() {
		super.onResume();
		PersonalLayoutManager.setColorForId(this, R.id.tvTermineLectureName);
	}
}
