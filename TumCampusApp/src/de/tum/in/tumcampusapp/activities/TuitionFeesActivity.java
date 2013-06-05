package de.tum.in.tumcampusapp.activities;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.ActivityForAccessingTumOnline;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.models.TuitionList;
import de.tum.in.tumcampusapp.tumonline.TUMOnlineRequestFetchListener;

/**
 * Activity to show the user's tuition ; based on grades.java / quick solution
 * 
 * @author NTK
 */
public class TuitionFeesActivity extends ActivityForAccessingTumOnline implements TUMOnlineRequestFetchListener {

	private TextView amountTextView;

	private TextView deadlineTextView;
	private TextView semesterTextView;
	/**
	 * tuition information
	 */
	private TuitionList tuitionList;
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
	}

	/**
	 * Handle the response by deserializing it into model entities.
	 * 
	 * @param rawResp
	 */
	@Override
	public void onFetch(String rawResp) {

		Serializer serializer = new Persister();
		tuitionList = null;

		try {
			tuitionList = serializer.read(TuitionList.class, rawResp);

			amountTextView.setText(tuitionList.getTuitions().get(0).getSoll() + "€");
			deadlineTextView.setText(tuitionList.getTuitions().get(0).getFrist());
			semesterTextView.setText(tuitionList.getTuitions().get(0).getSemesterBez());

		} catch (Exception e) {
			Log.d("SIMPLEXML", "wont work: " + e.getMessage());
			errorLayout.setVisibility(View.VISIBLE);
			progressLayout.setVisibility(View.GONE);
			e.printStackTrace();
		}
		progressLayout.setVisibility(View.GONE);
	}
}
