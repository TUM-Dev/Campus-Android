package de.tum.in.tumcampusapp.activities;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.models.TuitionList;
import de.tum.in.tumcampusapp.tumonline.TUMOnlineRequest;
import de.tum.in.tumcampusapp.tumonline.TUMOnlineRequestFetchListener;

/**
 * Activity to show the user's tuition ; based on grades.java / quick solution
 * 
 * @author NTK
 */
public class TuitionFeesActivity extends Activity implements TUMOnlineRequestFetchListener {


	/**
	 * tuition information
	 */
	private TuitionList tuition;
	TextView soll;
	TextView frist;
	TextView semester;
	
	/**
	 * HTTP request handler to handle requests to TUMOnline
	 */
	private TUMOnlineRequest requestHandler;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_tuitionfees);
		
		soll = (TextView) findViewById(R.id.soll);
		frist = (TextView) findViewById(R.id.frist);
		semester = (TextView) findViewById(R.id.semester);

	}

	@Override
	public void onStart() {
		super.onStart();

		requestHandler = new TUMOnlineRequest(Const.STUDIENBEITRAGSTATUS, this);

		String accessToken = PreferenceManager.getDefaultSharedPreferences(this).getString(Const.ACCESS_TOKEN, null);
		if (accessToken != null) {
			fetchValues();
		}
	}

	/** Fetches all tuition information from TUMOnline. */
	public void fetchValues() {
		requestHandler.fetchInteractive(this, this);
	}

	/**
	 * Handle the response by deserializing it into model entities.
	 * 
	 * @param rawResp
	 */
	@Override
	public void onFetch(String rawResp) {

		Serializer serializer = new Persister();
		tuition = null;

		try {
			// deserialize XML response
			tuition = serializer.read(TuitionList.class, rawResp);

			// TODO nice layout + language strings etc.
			soll.setText(tuition.getTuitions().get(0).getSoll());
		    frist.setText(tuition.getTuitions().get(0).getFrist());
		    semester.setText(tuition.getTuitions().get(0).getSemesterBez());

		} catch (Exception e) {
			Log.d("SIMPLEXML", "wont work: " + e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public void onFetchError(String errorReason) {
		Utils.showLongCenteredToast(this, errorReason);
	}

	@Override
	public void onFetchCancelled() {
		finish();
	}

	@Override
	public void onCommonError(String errorReason) {
		// TODO Auto-generated method stub
		
	}

}
