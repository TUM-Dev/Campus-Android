package de.tum.in.tumcampus.activities;

import java.util.Collections;
import java.util.List;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.generic.ActivityForAccessingTumOnline;
import de.tum.in.tumcampus.adapters.LecturesSearchListAdapter;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.ImplicitCounter;
import de.tum.in.tumcampus.models.LecturesSearchRow;
import de.tum.in.tumcampus.models.LecturesSearchRowSet;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * This activity presents the users' lectures using the TUMOnline web service
 * the results can be filtered by the semester or all shown.
 * 
 * This activity uses the same models as FindLectures.
 * 
 * HINT: a TUMOnline access token is needed
 * 
 * 
 * needed/linked files:
 * 
 * res.layout.mylectures (Layout XML), models.FindLecturesRowSet,
 * models.FindLecturesListAdapter
 * 
 * @solves [M1] Meine Lehrveranstaltungen
 * @author Daniel G. Mayr
 */
public class LecturesPersonalActivity extends ActivityForAccessingTumOnline {

	/** filtered list which will be shown */
	LecturesSearchRowSet lecturesList = null;

	/** UI elements */
	private StickyListHeadersListView lvMyLecturesList;

	public LecturesPersonalActivity() {
		super(Const.LECTURES_PERSONAL, R.layout.activity_lecturespersonal);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// bind UI elements
		lvMyLecturesList = (StickyListHeadersListView) findViewById(R.id.lvMyLecturesList);

		super.requestFetch();
        //Counting the number of times that the user used this activity for intelligent reordering
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPrefs.getBoolean("implicitly_id", true))
        {
            ImplicitCounter.Counter("my_lectures_id", getApplicationContext());
        }
	}

	@Override
	public void onFetch(String rawResponse) {
		// deserialize the XML
		Serializer serializer = new Persister();
		try {
			lecturesList = serializer.read(LecturesSearchRowSet.class,
					rawResponse);
		} catch (Exception e) {
			Log.d("SIMPLEXML", "wont work: " + e.getMessage());
			progressLayout.setVisibility(View.GONE);
			failedTokenLayout.setVisibility(View.VISIBLE);
			e.printStackTrace();
		}

        List<LecturesSearchRow> lectures = lecturesList.getLehrveranstaltungen();

        // Sort lectures by semester id
        Collections.sort(lectures);
        setListView(lectures);
        progressLayout.setVisibility(View.GONE);
	}

	/**
	 * Sets all data concerning the FindLecturesListView.
	 * 
	 * @param lecturesList
	 *            filtered list of lectures
	 */
	private void setListView(List<LecturesSearchRow> lecturesList) {
		// set ListView to data via the FindLecturesListAdapter
		lvMyLecturesList.setAdapter(new LecturesSearchListAdapter(this,
				lecturesList));

		// handle on click events by showing its LectureDetails
		lvMyLecturesList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> a, View v, int position,
					long id) {
				Object o = lvMyLecturesList.getItemAtPosition(position);
				LecturesSearchRow item = (LecturesSearchRow) o;

				// set bundle for LectureDetails and show it
				Bundle bundle = new Bundle();
				// we need the stp_sp_nr
				bundle.putString("stp_sp_nr", item.getStp_sp_nr());
				Intent intent = new Intent(LecturesPersonalActivity.this,
						LecturesDetailsActivity.class);
				intent.putExtras(bundle);
				// start LectureDetails for given stp_sp_nr
				startActivity(intent);
			}
		});
	}
}
