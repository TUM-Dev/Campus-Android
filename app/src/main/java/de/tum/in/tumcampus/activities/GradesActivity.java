package de.tum.in.tumcampus.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.generic.ActivityForAccessingTumOnline;
import de.tum.in.tumcampus.adapters.ExamListAdapter;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.NetUtils;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.Exam;
import de.tum.in.tumcampus.models.ExamList;
import de.tum.in.tumcampus.tumonline.TUMOnlineConst;

/**
 * Activity to show the user's grades/exams passed.
 */
public class GradesActivity extends ActivityForAccessingTumOnline<ExamList> {
	private static int LAST_CHOICE = 0;
	private TextView average_tx;

	private double averageGrade;
	private String columnChartContent;

	private MenuItem columnMenuItem;
	private MenuItem pieMenuItem;

	private ExamList examList;

	private final NumberFormat format = NumberFormat.getInstance(Locale.FRANCE);
	private ListView lvGrades;
	private String pieChartContent;

	private Spinner spFilter;

	private boolean isFetched;

	public GradesActivity() {
		super(TUMOnlineConst.EXAMS, R.layout.activity_grades);
	}

	/**
     * Builds HTML string showing a column chart
	 * 
	 * @param filteredExamList List of exams
	 * @return content string
	 */
    String buildColumnChartContentString(List<Exam> filteredExamList) {
		HashMap<String, Integer> gradeDistrubution_hash = calculateGradeDistribution(filteredExamList);

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
        return "<html>"
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
	}

	/**
     * Builds HTML string showing a pie chart
	 * 
	 * @param filteredExamList List of exams
	 * @return content string
	 */
    String buildPieChartContentString(List<Exam> filteredExamList) {
		HashMap<String, Integer> gradeDistrubution_hash = calculateGradeDistribution(filteredExamList);
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

        return "<html>"
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
	}

	/**
	 * Calculates the average grade of the given exams
	 * 
	 * @param filteredExamList List of exams
	 * @return Average grade
	 */
    Double calculateAverageGrade(List<Exam> filteredExamList) {
		List<Exam> removedDoubles = removeDuplicates(filteredExamList);
		double weightedGrade = 0.0;
		double creditSum = 0.0;

        for (Exam item : removedDoubles) {
            creditSum += Double.valueOf(item.getCredits());
            try {
                weightedGrade += format.parse(item.getGrade()).doubleValue()
                        * Double.valueOf(item.getCredits());
            } catch (NumberFormatException e) {
                Utils.log(e);
            } catch (ParseException e) {
                Utils.log(e);
            }

        }
		return weightedGrade / creditSum;

	}

	/**
	 * Calculates grade distribution
	 * 
	 * @param filteredExamList List of exams
	 * @return HashMap with grade to grade count mapping
	 */
    HashMap<String, Integer> calculateGradeDistribution(
            List<Exam> filteredExamList) {
		HashMap<String, Integer> gradeDistrubution_hash = new HashMap<String, Integer>();
        for (Exam item : filteredExamList) {
            // increment hash value
            int curCount = gradeDistrubution_hash.containsKey(item.getGrade()) ? gradeDistrubution_hash
                    .get(item.getGrade()) : 0;

            gradeDistrubution_hash.put(item.getGrade(), curCount + 1);
        }
		return gradeDistrubution_hash;
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

				if (filter.equals(getString(R.string.all_programs))) {

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

					averageGrade = Math.round(calculateAverageGrade(filteredExamList) * 1000.0) / 1000.0;

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

		requestFetch();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.menu_activity_grades, menu);
		columnMenuItem = menu.findItem(R.id.columnChart);
		pieMenuItem = menu.findItem(R.id.pieChart);

        // Both features (Chart diagrams) are not available on older devices
        if (android.os.Build.VERSION.SDK_INT < 11) {
            columnMenuItem.setVisible(false);
            pieMenuItem.setVisible(false);
        }
		return true;
	}

	/**
	 * Handle the response by de-serializing it into model entities.
	 *
	 * @param rawResponse Raw text response
	 */
	@Override
	public void onFetch(ExamList rawResponse) {
		examList = rawResponse;

        // initialize the program choice spinner
        initSpinner();

        // Displays results in view
        lvGrades.setAdapter(new ExamListAdapter(this, examList.getExams()));

        showLoadingEnded();

        // enabling the Menu options after first fetch
        isFetched = true;

        // update the action bar to display the enabled menu options
        if (Build.VERSION.SDK_INT >= 11) {
            invalidateOptionsMenu();
        }
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;

		if (NetUtils.isConnected(this)) {
			switch (item.getItemId()) {
			case R.id.columnChart:
				intent = new Intent(this, GradeChartActivity.class);
				intent.putExtra("chartContent", columnChartContent);
				startActivity(intent);
				return true;
			case R.id.pieChart:
				intent = new Intent(this, GradeChartActivity.class);
				intent.putExtra("chartContent", pieChartContent);
				startActivity(intent);
				return true;
			default:
				isFetched = false;
				return super.onOptionsItemSelected(item);
			}
		} else {
			showError(R.string.no_internet_connection);
			average_tx.setVisibility(View.GONE);
			return true;
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// enable Menu Items after fetching grades
		columnMenuItem = menu.findItem(R.id.columnChart);
        columnMenuItem.setEnabled(isFetched);
		pieMenuItem = menu.findItem(R.id.pieChart);
        pieMenuItem.setEnabled(isFetched);

		return super.onPrepareOptionsMenu(menu);
	}

	/**
	 * Removes duplicate exams from the list
	 * 
	 * @param filteredExamList List of exams
	 * @return List with duplicate items removed
	 */
    List<Exam> removeDuplicates(List<Exam> filteredExamList) {
		List<Exam> removedDoubles = new ArrayList<Exam>();

		// find and remove duplicates
		for (int i = 0; i < filteredExamList.size(); i++) {
			Exam item_one = filteredExamList.get(i);
			boolean insert = true;

            for (Exam item_two : filteredExamList) {
                if (item_one.getCourse().equals(item_two.getCourse())) {
                    Utils.logv("Double = " + item_one.getCourse());
                    try {
                        if (format.parse(item_one.getGrade()).doubleValue() > format
                                .parse(item_two.getGrade()).doubleValue())
                            insert = false;
                    } catch (ParseException e) {
                        Utils.log(e);
                    }
                }
            }

			if (insert)
				removedDoubles.add(item_one);
		}
		return removedDoubles;
	}
}
