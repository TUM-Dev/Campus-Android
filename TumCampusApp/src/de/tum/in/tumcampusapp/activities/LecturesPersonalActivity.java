package de.tum.in.tumcampusapp.activities;

import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.TumOnlineActivity;
import de.tum.in.tumcampusapp.adapters.LecturesSearchListAdapter;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.models.managers.LecturesSearchRow;
import de.tum.in.tumcampusapp.models.managers.LecturesSearchRowSet;
import de.tum.in.tumcampusapp.tumonline.TUMOnlineRequest;

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
public class LecturesPersonalActivity extends TumOnlineActivity {
	
	public LecturesPersonalActivity() {
		super(Const.LECTURES_PERSONAL, R.layout.activity_lecturespersonal);
	}

	/** filtered list which will be shown */
	LecturesSearchRowSet lecturesList = null;

	/** UI elements */
	private ListView lvMyLecturesList;
	private Spinner spFilter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// bind UI elements
		lvMyLecturesList = (ListView) findViewById(R.id.lvMyLecturesList);
		spFilter = (Spinner) findViewById(R.id.spFilter);
		
		requestFetch();
	}

	@Override
	public void onFetch(String rawResponse) {

		// deserialize the XML
		Serializer serializer = new Persister();
		try {
			lecturesList = serializer.read(LecturesSearchRowSet.class, rawResponse);
		} catch (Exception e) {
			Log.d("SIMPLEXML", "wont work: " + e.getMessage());
			errorLayout.setVisibility(View.VISIBLE);
			progressLayout.setVisibility(View.GONE);
			e.printStackTrace();
		}

		// set Spinner data (semester)
		List<String> filters = new ArrayList<String>();

		try { // NTK quickfix

			filters.add(getString(R.string.all));
			for (int i = 0; i < lecturesList.getLehrveranstaltungen().size(); i++) {
				String item = lecturesList.getLehrveranstaltungen().get(i).getSemester_id();
				if (filters.indexOf(item) == -1) {
					filters.add(item);
				}
			}

			// simple adapter for the spinner
			ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, filters);
			spFilter.setAdapter(spinnerArrayAdapter);
			spFilter.setOnItemSelectedListener(new OnItemSelectedListener() {

				/**
				 * if an item in the spinner is selected, we have to filter the
				 * results which are displayed in the ListView
				 * 
				 * -> tList will be the data which will be passed to the
				 * FindLecturesListAdapter
				 */
				@Override
				public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
					String filter = spFilter.getItemAtPosition(arg2).toString();
					if (filter == getString(R.string.all)) {
						setListView(lecturesList.getLehrveranstaltungen());
					} else {
						// do filtering for the given semester
						List<LecturesSearchRow> filteredList = new ArrayList<LecturesSearchRow>();
						for (int i = 0; i < lecturesList.getLehrveranstaltungen().size(); i++) {
							LecturesSearchRow item = lecturesList.getLehrveranstaltungen().get(i);
							if (item.getSemester_id().equals(filter)) {
								filteredList.add(item);
							}
						}
						// listview gets filtered list
						setListView(filteredList);
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					// select [Alle], if none selected either
					spFilter.setSelection(0);
					setListView(lecturesList.getLehrveranstaltungen());
				}
			});

			setListView(lecturesList.getLehrveranstaltungen());
			progressLayout.setVisibility(View.GONE);

		} catch (Exception e) { // NTK quickfix
			Log.e("TumCampus", "No lectures available - Error: " + e.getMessage());
			Toast err = Toast.makeText(this, "No lectures available - press back-button", Toast.LENGTH_LONG);
			err.show();
		}
	}

	/**
	 * Sets all data concerning the FindLecturesListView.
	 * 
	 * @param lecturesList
	 *            filtered list of lectures
	 */
	private void setListView(List<LecturesSearchRow> lecturesList) {
		// set ListView to data via the FindLecturesListAdapter
		lvMyLecturesList.setAdapter(new LecturesSearchListAdapter(this, lecturesList));

		// handle on click events by showing its LectureDetails
		lvMyLecturesList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> a, View v, int position, long id) {
				Object o = lvMyLecturesList.getItemAtPosition(position);
				LecturesSearchRow item = (LecturesSearchRow) o;

				// set bundle for LectureDetails and show it
				Bundle bundle = new Bundle();
				// we need the stp_sp_nr
				bundle.putString("stp_sp_nr", item.getStp_sp_nr());
				Intent i = new Intent(LecturesPersonalActivity.this, LecturesDetailsActivity.class);
				i.putExtras(bundle);
				// start LectureDetails for given stp_sp_nr
				startActivity(i);
			}
		});
	}
}
