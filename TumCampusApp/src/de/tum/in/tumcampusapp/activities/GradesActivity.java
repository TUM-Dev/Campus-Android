package de.tum.in.tumcampusapp.activities;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.ActivityForAccessingTumOnline;
import de.tum.in.tumcampusapp.adapters.ExamListAdapter;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.models.Exam;
import de.tum.in.tumcampusapp.models.ExamList;

/**
 * Activity to show the user's grades/exams passed.
 * 
 * @author Vincenz Doelle
 * @review Daniel G. Mayr
 * @review Thomas Behrens
 */
public class GradesActivity extends ActivityForAccessingTumOnline {

	private ExamList examList;
	private ListView lvGrades;

	public GradesActivity() {
		super(Const.NOTEN, R.layout.activity_grades);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		lvGrades = (ListView) findViewById(R.id.lstGrades);

		super.requestFetch();
	}

	/**
	 * Handle the response by deserializing it into model entities.
	 * 
	 * @param rawResponse
	 */
	@Override
	public void onFetch(String rawResponse) {
		Log.d(getClass().getSimpleName(), rawResponse);

		Serializer serializer = new Persister();
		examList = null;

		try {
			// Deserializes XML response
			examList = serializer.read(ExamList.class, rawResponse);

			// Displays results in view
			lvGrades.setAdapter(new ExamListAdapter(GradesActivity.this,
					examList.getExams()));

			// handle on click events by showing its LectureDetails
			lvGrades.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> a, View v, int position,
						long id) {
					Object o = lvGrades.getItemAtPosition(position);
					Exam item = (Exam) o;
					String progId = item.getProgramID();
					Log.d(getClass().getSimpleName(), progId);

					// set bundle for LectureDetails and show it
					Bundle bundle = new Bundle();
					// we need the stp_sp_nr
					bundle.putString("stp_sp_nr", progId);
					Intent i = new Intent(GradesActivity.this,
							LecturesDetailsActivity.class);
					i.putExtras(bundle);
					// start LectureDetails for given stp_sp_nr
					startActivity(i);
				}
			});

			progressLayout.setVisibility(View.GONE);

		} catch (Exception e) {
			Log.d("SIMPLEXML", "wont work: " + e.getMessage());
			progressLayout.setVisibility(View.GONE);
			failedTokenLayout.setVisibility(View.VISIBLE);
			e.printStackTrace();
		}
	}
}
