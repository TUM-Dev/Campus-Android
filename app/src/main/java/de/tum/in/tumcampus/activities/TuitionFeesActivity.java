package de.tum.in.tumcampus.activities;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.generic.ActivityForAccessingTumOnline;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.ImplicitCounter;
import de.tum.in.tumcampus.models.TuitionList;
import de.tum.in.tumcampus.tumonline.TUMOnlineRequestFetchListener;

/**
 * Activity to show the user's tuition ; based on grades.java / quick solution
 * 
 * @author NTK
 */
public class TuitionFeesActivity extends ActivityForAccessingTumOnline
		implements TUMOnlineRequestFetchListener {

	private TextView amountTextView;
	private TextView deadlineTextView;
	private TextView semesterTextView;

    public TuitionFeesActivity() {
		super(Const.STUDIENBEITRAGSTATUS, R.layout.activity_tuitionfees);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		amountTextView = (TextView) findViewById(R.id.soll);
		deadlineTextView = (TextView) findViewById(R.id.frist);
		semesterTextView = (TextView) findViewById(R.id.semester);

		super.requestFetch();

		//Counting the number of times that the user used this activity for intelligent reordering 
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		if (sharedPrefs.getBoolean("implicitly_id", true)){
				ImplicitCounter.Counter("tuition_fees_id", getApplicationContext());
		}
	}

	/**
	 * Handle the response by deserializing it into model entities.
	 * 
	 * @param rawResp
	 */
	@SuppressLint("DefaultLocale")
	@Override
	public void onFetch(String rawResp) {

		Serializer serializer = new Persister();
		/*
	  tuition information
	 */
        TuitionList tuitionList = null;

		try {
			tuitionList = serializer.read(TuitionList.class, rawResp);

			amountTextView.setText(tuitionList.getTuitions().get(0).getSoll()
					+ "€");
			deadlineTextView.setText(tuitionList.getTuitions().get(0)
					.getFrist());
			semesterTextView.setText(tuitionList.getTuitions().get(0)
					.getSemesterBez().toUpperCase());

		} catch (Exception e) {
			Log.d("SIMPLEXML", "wont work: " + e.getMessage());
			errorLayout.setVisibility(View.VISIBLE);
			progressLayout.setVisibility(View.GONE);
			e.printStackTrace();
		}
		progressLayout.setVisibility(View.GONE);
	}
}
