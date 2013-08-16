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
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.ActivityForAccessingTumOnline;
import de.tum.in.tumcampusapp.adapters.ExamListAdapter;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.Dialogs;
import de.tum.in.tumcampusapp.auxiliary.PersonalLayoutManager;
import de.tum.in.tumcampusapp.auxiliary.Utils;
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
	static int LAST_CHOICE = 0;
	boolean allSelected = false;
	private TextView average_tx;

	private double averageGrade;
	private String columnChartContent;

	private MenuItem columnMenuItem;
	private MenuItem pieMenuItem;

	private ExamList examList;

	// private HashMap<String, Integer> gradeDistrubution_hash;
	private final NumberFormat format = NumberFormat.getInstance(Locale.FRANCE);
	private ListView lvGrades;
	private String pieChartContent;

	private Spinner spFilter;

	private boolean isFetched;

	public GradesActivity() {
		super(Const.NOTEN, R.layout.activity_grades);
	}

	public HashMap<String, Integer> calcGradeDistribution(
			List<Exam> filteredExamList) {
		HashMap<String, Integer> gradeDistrubution_hash = new HashMap<String, Integer>();
		for (int j = 0; j < filteredExamList.size(); j++) {
			Exam item = filteredExamList.get(j);

			// increment hash value
			int curCount = gradeDistrubution_hash.containsKey(item.getGrade()) ? gradeDistrubution_hash
					.get(item.getGrade()) : 0;

			gradeDistrubution_hash.put(item.getGrade(), curCount + 1);

		}
		return gradeDistrubution_hash;

	}

	public String buildColumnChartContentString(List<Exam> filteredExamList) {
		HashMap<String, Integer> gradeDistrubution_hash = calcGradeDistribution(filteredExamList);

		String datas = "";
		// Build data string
		for (int i = 0; i < Const.GRADES.length; i++) {
			if (i == Const.GRADES.length - 1)
				datas += "['" + Const.GRADES[i] + "', "
						+ gradeDistrubution_hash.get(Const.GRADES[i]) + "]";
			else
				datas += "['" + Const.GRADES[i] + "', "
						+ gradeDistrubution_hash.get(Const.GRADES[i]) + "],";
		}

		// Build content String
		String content = "<html>"
				+ "  <head>"
				+ "    <script type=\"text/javascript\" src=\"jsapi.js\"></script>"
				+ "    <script type=\"text/javascript\">"
				+ "      google.load(\"visualization\", \"1\", {packages:[\"corechart\"]});"
				+ "      google.setOnLoadCallback(drawChart);"
				+ "      function drawChart() {"
				+ "        var data = google.visualization.arrayToDataTable(["
				+ "          ['Grade', 'Quantity'],"
				+ datas
				+ "        ]);"
				+ "        var options = {"
				+ "          title: 'Grades of "
				+ filteredExamList.get(0).getProgramID()
				+ "',"
				// + " 	     legend: {position: 'none'}"
				+ "        };"
				+ "        var chart = new google.visualization.ColumnChart(document.getElementById('chart_div'));"
				+ "        chart.draw(data, options);"
				+ "      }"
				+ "    </script>"
				+ "  </head>"
				+ "  <body>"
				+ "    <div id=\"chart_div\" style=\"width: 1000px; height: 500px;\"></div>"
				+ "  </body>" + "</html>";

		return content;
	}

	public String buildPieChartContentString(List<Exam> filteredExamList) {
		HashMap<String, Integer> gradeDistrubution_hash = calcGradeDistribution(filteredExamList);
		String datas = "";

		// build data String
		for (int i = 0; i < Const.GRADES.length; i++) {
			if (i == Const.GRADES.length - 1) {
				datas += "['" + Const.GRADES[i] + "', "
						+ gradeDistrubution_hash.get(Const.GRADES[i]) + "]";
			} else {
				datas += "['" + Const.GRADES[i] + "', "
						+ gradeDistrubution_hash.get(Const.GRADES[i]) + "],";

			}
		}
		// build content String
		String content = "<html>"
				+ "  <head>"
				+ "    <script type=\"text/javascript\" src=\"jsapi.js\"></script>"
				+ "    <script type=\"text/javascript\">"
				+ "      google.load(\"visualization\", \"1\", {packages:[\"corechart\"]});"
				+ "      google.setOnLoadCallback(drawChart);"
				+ "      function drawChart() {"
				+ "        var data = google.visualization.arrayToDataTable(["
				+ "          ['Grade', 'Quantity'],"
				+ datas
				+ "        ]);"
				+ "        var options = {"
				+ "          title: 'Grades of "
				+ filteredExamList.get(0).getProgramID()
				+ "'"
				+ "        };"
				+ "        var chart = new google.visualization.PieChart(document.getElementById('chart_div'));"
				+ "        chart.draw(data, options);"
				+ "      }"
				+ "    </script>"
				+ "  </head>"
				+ "  <body>"
				+ "    <div id=\"chart_div\" style=\"width: 1000px; height: 500px;\"></div>"
				+ "  </body>" + "</html>";

		return content;
	}

	public Double calculateAverageGrade(List<Exam> filteredExamList) {
		List<Exam> removedDoubles = removeDuplicates(filteredExamList);
		double weightedGrade = 0.0;
		double creditSum = 0.0;

		for (int i = 0; i < removedDoubles.size(); i++) {
			Exam item = removedDoubles.get(i);
			creditSum += Double.valueOf(item.getCredits());
			try {
				weightedGrade += format.parse(item.getGrade()).doubleValue()
						* Double.valueOf(item.getCredits());
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			}

		}
		return weightedGrade / creditSum;

	}

	/**
	 * Initialize the spinner for choosing between the study programs.
	 */
	private void initSpinner() {

		// set Spinner data
		List<String> filters = new ArrayList<String>();
		filters.add(getString(R.string.all_programs));

		// get all program ids from the results
		for (int i = 0; i < examList.getExams().size(); i++) {
			String item = examList.getExams().get(i).getProgramID();
			if (filters.indexOf(item) == -1) {
				filters.add(item);
			}

		}

		// init the spinner
		ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
				this, android.R.layout.simple_list_item_checked, filters);
		spFilter.setAdapter(spinnerArrayAdapter);
		spFilter.setSelection(LAST_CHOICE);

		// handle if program choice is changed
		spFilter.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {

				String filter = spFilter.getItemAtPosition(arg2).toString();
				LAST_CHOICE = arg2;

				if (filter == getString(R.string.all_programs)) {

					// display all grades
					lvGrades.setAdapter(new ExamListAdapter(
							GradesActivity.this, examList.getExams()));
					average_tx.setVisibility(View.GONE);
					// convert examlist
					List<Exam> convertedList = new ArrayList<Exam>();
					for (int i = 0; i < examList.getExams().size(); i++) {
						Exam item = examList.getExams().get(i);
						convertedList.add(item);
					}

					// build chart Content for corresponding list
					columnChartContent = buildColumnChartContentString(convertedList);
					pieChartContent = buildPieChartContentString(convertedList);

				} else {
					// do filtering according to selected program
					List<Exam> filteredExamList = new ArrayList<Exam>();
					for (int i = 0; i < examList.getExams().size(); i++) {
						Exam item = examList.getExams().get(i);
						if (item.getProgramID().equals(filter)) {
							filteredExamList.add(item);
						}

					}
					// list view gets filtered list
					lvGrades.setAdapter(new ExamListAdapter(
							GradesActivity.this, filteredExamList));

					columnChartContent = buildColumnChartContentString(filteredExamList);
					pieChartContent = buildPieChartContentString(filteredExamList);

					averageGrade = Math
							.round(calculateAverageGrade(filteredExamList) * 1000.0) / 1000.0;

					average_tx.setText(getResources().getString(
							R.string.average_grade)
							+ ": " + averageGrade);
					average_tx.setVisibility(View.VISIBLE);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// select ALL
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.menu_activity_grades, menu);
		columnMenuItem = menu.findItem(R.id.columnChart);
		pieMenuItem = menu.findItem(R.id.pieChart);
		return true;
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

			// enabling the Menu options after first fetch
			isFetched = true;

			// update the action bar to display the enabled menu options
			if (Build.VERSION.SDK_INT >= 11)
				invalidateOptionsMenu();

		} catch (Exception e) {
			Log.d("SIMPLEXML", "wont work: " + e.getMessage());
			progressLayout.setVisibility(View.GONE);
			failedTokenLayout.setVisibility(View.VISIBLE);
			e.printStackTrace();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;

		// Both features (Chart diagrams) are not available on older devices
		if (android.os.Build.VERSION.SDK_INT <= 10) {
			Dialogs.showAndroidVersionTooLowAlert(this);
			return true;
		}

		if (Utils.isConnected(this)) {
			switch (item.getItemId()) {
			case R.id.columnChart:
				intent = new Intent(GradesActivity.this,
						GradeChartActivity.class);
				intent.putExtra("chartContent", columnChartContent);
				startActivity(intent);
				return true;
			case R.id.pieChart:
				intent = new Intent(GradesActivity.this,
						GradeChartActivity.class);
				intent.putExtra("chartContent", pieChartContent);
				startActivity(intent);
				return true;

			default:
				isFetched = false;
				return super.onOptionsItemSelected(item);
			}
		} else {
			Toast.makeText(this, R.string.no_internet_connection,
					Toast.LENGTH_SHORT).show();
			errorLayout.setVisibility(View.VISIBLE);
			return true;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		PersonalLayoutManager.setColorForId(this, R.id.spFilter);
		PersonalLayoutManager.setColorForId(this, R.id.avgGrade);
	}

	public List<Exam> removeDuplicates(List<Exam> filteredExamList) {
		List<Exam> removedDoubles = new ArrayList<Exam>();

		// find and remove duplicates
		for (int i = 0; i < filteredExamList.size(); i++) {
			Exam item_one = filteredExamList.get(i);
			boolean insert = true;

			for (int j = 0; j < filteredExamList.size(); j++) {
				Exam item_two = filteredExamList.get(j);
				if (item_one.getCourse().equals(item_two.getCourse())) {
					Log.d("Double = ", item_one.getCourse());
					try {
						if (format.parse(item_one.getGrade()).doubleValue() > format
								.parse(item_two.getGrade()).doubleValue())
							insert = false;
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

			if (insert)
				removedDoubles.add(item_one);
		}
		return removedDoubles;
	}
	
	public void setMenuEnabled(boolean enabled) {
		columnMenuItem.setEnabled(enabled);
		pieMenuItem.setEnabled(enabled);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// enable Menu Items after fetching grades
		columnMenuItem = menu.findItem(R.id.columnChart);
		pieMenuItem = menu.findItem(R.id.pieChart);
		setMenuEnabled(isFetched);
		return super.onPrepareOptionsMenu(menu);
	}
}
