package de.tum.in.tumcampusapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
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
import java.util.Map;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.ActivityForAccessingTumOnline;
import de.tum.in.tumcampusapp.adapters.ExamListAdapter;
import de.tum.in.tumcampusapp.auxiliary.NetUtils;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.models.tumo.Exam;
import de.tum.in.tumcampusapp.models.tumo.ExamList;
import de.tum.in.tumcampusapp.tumonline.TUMOnlineConst;

/**
 * Activity to show the user's grades/exams passed.
 */
public class GradesActivity extends ActivityForAccessingTumOnline<ExamList> {
    private static final String[] GRADES = {"1,0", "1,3", "1,4", "1,7", "2,0", "2,3", "2,4", "2,7", "3,0", "3,3", "3,4", "3,7", "4,0", "4,3", "4,4", "4,7", "5,0"};
    private final NumberFormat format = NumberFormat.getInstance(Locale.FRANCE);
    private static int lastChoice;
    private TextView averageTx;
    private double averageGrade;
    private String columnChartContent;
    private MenuItem columnMenuItem;
    private MenuItem pieMenuItem;
    private ExamList examList;
    private ListView lvGrades;
    private String pieChartContent;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private Spinner spFilter;

    private boolean isFetched;

    public GradesActivity() {
        super(TUMOnlineConst.Companion.getEXAMS(), R.layout.activity_grades);
    }

    /**
     * Builds HTML string showing a column chart
     *
     * @param filteredExamList List of exams
     * @return content string
     */
    String buildColumnChartContentString(List<Exam> filteredExamList) {
        Map<String, Integer> gradeDistribution = calculateGradeDistribution(filteredExamList);

        StringBuilder datas = new StringBuilder(1024);
        // Build data string
        for (int i = 0; i < GRADES.length; i++) {
            datas.append("['")
                 .append(GRADES[i])
                 .append("', ")
                 .append(gradeDistribution.get(GRADES[i]))
                 .append(']');

            if (i != GRADES.length - 1) {
                datas.append(',');
            }
        }

        // Build content String
        return "<html>"
               + "  <head>"
               + "    <script type=\"text/javascript\" src=\"https://www.google.com/jsapi\"></script>"
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
               + filteredExamList.get(0)
                                 .getProgramID()
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
        Map<String, Integer> gradeDistrubution = calculateGradeDistribution(filteredExamList);
        StringBuilder datas = new StringBuilder(1024);

        // build data String
        for (int i = 0; i < GRADES.length; i++) {
            datas.append("['")
                 .append(GRADES[i])
                 .append("', ")
                 .append(gradeDistrubution.get(GRADES[i]))
                 .append(']');
            if (i != GRADES.length - 1) {
                datas.append(',');
            }
        }

        // build content String

        return "<html>"
               + "  <head>"
               + "    <script type=\"text/javascript\" src=\"https://www.google.com/jsapi\"></script>"
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
               + filteredExamList.get(0)
                                 .

                                         getProgramID()

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
        //List<Exam> removedDoubles = removeDuplicates(filteredExamList);
        double weightedGrade = 0.0;
        double creditSum = 0.0;

        for (Exam item : filteredExamList) {
            creditSum += Double.valueOf(item.getCredits());
            try {
                weightedGrade += format.parse(item.getGrade())
                                       .doubleValue()
                                 * Double.valueOf(item.getCredits());
            } catch (NumberFormatException | ParseException e) {
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
    Map<String, Integer> calculateGradeDistribution(
            List<Exam> filteredExamList) {
        Map<String, Integer> gradeDistribution = new HashMap<>(128);
        for (Exam item : filteredExamList) {
            // increment hash value
            int curCount = gradeDistribution.containsKey(item.getGrade()) ? gradeDistribution
                    .get(item.getGrade()) : 0;

            gradeDistribution.put(item.getGrade(), curCount + 1);
        }
        return gradeDistribution;
    }

    /**
     * Initialize the spinner for choosing between the study programs.
     */
    private void initSpinner() {

        // set Spinner data
        List<String> filters = new ArrayList<>();
        filters.add(getString(R.string.all_programs));

        // get all program ids from the results
        for (int i = 0; i < examList.getExams()
                                    .size(); i++) {
            String item = examList.getExams()
                                  .get(i)
                                  .getProgramID();
            if (filters.indexOf(item) == -1) {
                filters.add(item);
            }
        }

        // init the spinner
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_checked, filters);
        spFilter.setAdapter(spinnerArrayAdapter);
        spFilter.setSelection(lastChoice);

        // handle if program choice is changed
        spFilter.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

                String filter = spFilter.getItemAtPosition(arg2)
                                        .toString();
                lastChoice = arg2;

                if (filter.equals(getString(R.string.all_programs))) {

                    // display all grades
                    lvGrades.setAdapter(new ExamListAdapter(
                            GradesActivity.this, examList.getExams()));
                    averageTx.setVisibility(View.GONE);
                    // convert exam list
                    List<Exam> convertedList = new ArrayList<>();
                    for (int i = 0; i < examList.getExams()
                                                .size(); i++) {
                        Exam item = examList.getExams()
                                            .get(i);
                        convertedList.add(item);
                    }

                    // build chart Content for corresponding list
                    columnChartContent = buildColumnChartContentString(convertedList);
                    pieChartContent = buildPieChartContentString(convertedList);

                } else {
                    // do filtering according to selected program
                    List<Exam> filteredExamList = new ArrayList<>();
                    for (int i = 0; i < examList.getExams()
                                                .size(); i++) {
                        Exam item = examList.getExams()
                                            .get(i);
                        if (item.getProgramID()
                                .equals(filter)) {
                            filteredExamList.add(item);
                        }

                    }
                    // list view gets filtered list
                    lvGrades.setAdapter(new ExamListAdapter(
                            GradesActivity.this, filteredExamList));

                    columnChartContent = buildColumnChartContentString(filteredExamList);
                    pieChartContent = buildPieChartContentString(filteredExamList);

                    averageGrade = Math.round(calculateAverageGrade(filteredExamList) * 1000.0) / 1000.0;

                    averageTx.setText(String.format("%s: %s",
                                                    getResources().getString(R.string.average_grade), averageGrade));
                    averageTx.setVisibility(View.VISIBLE);
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

        lvGrades = findViewById(R.id.lstGrades);
        spFilter = findViewById(R.id.spFilter);
        averageTx = findViewById(R.id.avgGrade);
        mSwipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.color_primary, R.color.tum_A100, R.color.tum_A200);

        requestFetch();
    }

    @Override
    protected void requestFetch() {
        super.requestFetch();
        mSwipeRefreshLayout.setRefreshing(true);
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
        this.invalidateOptionsMenu();
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onFetchError(String errorReason) {
        super.onFetchError(errorReason);
        Utils.log("Noten failed due to: " + errorReason);
    }

    @Override
    public void onNoDataToShow() {
        showError(R.string.no_grades);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;

        if (NetUtils.isConnected(this)) {
            int i = item.getItemId();
            if (i == R.id.columnChart) {
                intent = new Intent(this, GradeChartActivity.class);
                intent.putExtra("chartContent", columnChartContent);
                startActivity(intent);
                return true;
            } else if (i == R.id.pieChart) {
                intent = new Intent(this, GradeChartActivity.class);
                intent.putExtra("chartContent", pieChartContent);
                startActivity(intent);
                return true;
            } else {
                isFetched = false;
                return super.onOptionsItemSelected(item);
            }
        } else {
            showError(R.string.no_internet_connection);
            averageTx.setVisibility(View.GONE);
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

    List<Exam> removeDuplicates(List<Exam> filteredExamList) {
    List<Exam> removedDoubles = new ArrayList<>();

    // find and remove duplicates
    for (int i = 0; i < filteredExamList.size(); i++) {
    Exam item1 = filteredExamList.get(i);
    boolean insert = true;

    for (Exam item2 : filteredExamList) {
    if (item1.getCourse().equals(item2.getCourse())) {
    Utils.logv("Double = " + item1.getCourse());
    try {
    if (format.parse(item1.getGrade()).doubleValue() > format
    .parse(item2.getGrade()).doubleValue()) {
    insert = false;
    }
    } catch (ParseException e) {
    Utils.log(e);
    }
    }
    }

    if (insert) {
    removedDoubles.add(item1);
    }
    }
    return removedDoubles;
    }*/
}
