package de.tum.in.tumcampusapp.activities;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.adapters.LectureAppointmentsListAdapter;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.models.LectureAppointmentsRowSet;
import de.tum.in.tumcampusapp.tumonline.TUMOnlineRequest;
import de.tum.in.tumcampusapp.tumonline.TUMOnlineRequestFetchListener;

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
public class LecturesAppointmentsActivity extends Activity implements TUMOnlineRequestFetchListener {

	private static final String VERANSTALTUNGEN_TERMINE = "veranstaltungenTermine";

	/** UI elements */
	private ListView lvTermine;
	/** Handler to send request to TUMOnline */
	private TUMOnlineRequest requestHandler;

	private TextView tvTermineLectureName;

	@Override
	public void onCommonError(String errorReason) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lecturesappointments);

		// set UI Elements
		lvTermine = (ListView) findViewById(R.id.lvTerminList);
		tvTermineLectureName = (TextView) findViewById(R.id.tvTermineLectureName);

	}

	/** process data got from TUMOnline request and show the listview */
	@Override
	public void onFetch(String rawResponse) {
		// deserialize xml
		Serializer serializer = new Persister();
		LectureAppointmentsRowSet LecturesList = null;
		try {
			LecturesList = serializer.read(LectureAppointmentsRowSet.class, rawResponse);
		} catch (Exception e) {
			Log.d("SIMPLEXML", "wont work: " + e.getMessage());
			e.printStackTrace();
		}

		// may happen if there are no appointments for the lecture
		if (LecturesList == null) {
			return;
		}

		// set data to the ListView object
		// nothing to click (yet)
		lvTermine.setAdapter(new LectureAppointmentsListAdapter(this, LecturesList.getLehrveranstaltungenTermine()));
	}

	@Override
	public void onFetchCancelled() {
		// show toast to notice cancel
		Utils.showLongCenteredToast(this, getString(R.string.cancel));
	}

	/**
	 * while fetching a TUMOnline Request an error occurred this will show the
	 * error message in a toast
	 */
	@Override
	public void onFetchError(String errorReason) {
		Utils.showLongCenteredToast(this, errorReason);
	}

	@Override
	public void onStart() {
		super.onStart();

		// set all for request handler
		requestHandler = new TUMOnlineRequest(VERANSTALTUNGEN_TERMINE, this);
		Bundle bundle = this.getIntent().getExtras();
		// set Lecture Name (depends on bundle data)
		tvTermineLectureName.setText(bundle.getString(Const.TITLE_EXTRA));
		requestHandler.setParameter("pLVNr", bundle.getString("stp_sp_nr"));

		// start fetching data
		requestHandler.fetchInteractive(this, this);
	}

}
