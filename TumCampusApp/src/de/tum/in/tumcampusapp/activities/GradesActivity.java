package de.tum.in.tumcampusapp.activities;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

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
import android.widget.Spinner;
import android.widget.TextView;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.ActivityForAccessingTumOnline;
import de.tum.in.tumcampusapp.adapters.ExamListAdapter;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.PersonalLayoutManager;
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

	private TextView average_tx;
	private double averageGrade;
	private HashMap<String, Double> creditSum_hash;
	private ExamList examList;
	private ListView lvGrades;

	private Spinner spFilter;
	private HashMap<String, Double> weightedGrades_hash;

	public GradesActivity() {
		super(Const.NOTEN, R.layout.activity_grades);
	}

	/**
	 * Initialize the spinner for choosing between the study programs.
	 */
	private void initSpinner() {

		// set Spinner data
		List<String> filters = new ArrayList<String>();
		filters.add(getString(R.string.all_programs));

		// initialize hashmaps with programm_ids as keys

		weightedGrades_hash = new HashMap<String, Double>();
		creditSum_hash = new HashMap<String, Double>();

		// get all program ids from the results
		for (int i = 0; i < examList.getExams().size(); i++) {
			String item = examList.getExams().get(i).getProgramID();
			if (filters.indexOf(item) == -1) {
				filters.add(item);
			}
			// init HashMap with 0.0 for each key
			weightedGrades_hash.put(item, 0.0);
			creditSum_hash.put(item, 0.0);
		}

		// init the spinner
		ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
				this, android.R.layout.simple_list_item_checked, filters);
		spFilter.setAdapter(spinnerArrayAdapter);

		// handle if program choice is changed
		spFilter.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {

				String filter = spFilter.getItemAtPosition(arg2).toString();

				if (filter == getString(R.string.all_programs)) {
					// display all grades
					lvGrades.setAdapter(new ExamListAdapter(
							GradesActivity.this, examList.getExams()));
					average_tx.setVisibility(View.GONE);

				} else {
					// do filtering according to selected program
					List<Exam> filteredExamList = new ArrayList<Exam>();
					for (int i = 0; i < examList.getExams().size(); i++) {
						Exam item = examList.getExams().get(i);
						if (item.getProgramID().equals(filter)) {
							filteredExamList.add(item);
						}

						// calculate average grade for every programm
						NumberFormat format = NumberFormat
								.getInstance(Locale.FRANCE); // to parse the
																// number from
																// x,y to x.y

						String curKey = item.getProgramID();
						double curGrade = weightedGrades_hash.get(curKey);
						double curSum = creditSum_hash.get(curKey);
						try {
							weightedGrades_hash.put(
									item.getProgramID(),
									curGrade
											+ (format.parse(item.getGrade()))
													.doubleValue()
											* Double.valueOf(item.getCredits()));
						} catch (NumberFormatException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						creditSum_hash.put(curKey,
								curSum + Double.valueOf(item.getCredits()));
					}
					// list view gets filtered list
					lvGrades.setAdapter(new ExamListAdapter(
							GradesActivity.this, filteredExamList));

					// round and display average grade
					averageGrade = Math.round(weightedGrades_hash.get(filter)
							/ creditSum_hash.get(filter) * 1000.0) / 1000.0;

					average_tx.setText(getResources().getString(
							R.string.average_grade)
							+ ":" + averageGrade);
					average_tx.setVisibility(View.VISIBLE);
				}

			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// select [Alle]
				spFilter.setSelection(0);
				lvGrades.setAdapter(new ExamListAdapter(GradesActivity.this,
						examList.getExams()));
			}
		});
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		lvGrades = (ListView) findViewById(R.id.lstGrades);
		spFilter = (Spinner) findViewById(R.id.spFilter);
		average_tx = (TextView) findViewById(R.id.avgGrade);
		average_tx.setVisibility(View.GONE);

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

			// initialize the program choice spinner
			initSpinner();

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

	@Override
	protected void onResume() {
		super.onResume();
		PersonalLayoutManager.setColorForId(this, R.id.spFilter);
		PersonalLayoutManager.setColorForId(this, R.id.avgGrade);
	}
}
