package de.tum.in.tumcampusapp.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

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
    private StickyListHeadersListView lvGrades;
    private WebView chartWebView;
    private String pieChartContent;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private boolean landscape, chartVisible;
    private int chartSize;

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
               + "    <script type=\"text/javascript\" src=\"https://www.gstatic.com/charts/loader.js\"></script>"
               + "    <script type=\"text/javascript\">"
               + "      google.load(\"visualization\", \"1\", {packages:[\"corechart\"]});"
               + "      google.setOnLoadCallback(drawChart);"
               + "      function drawChart() {"
               + "        var data = google.visualization.arrayToDataTable(["
               + "          ['Grade', 'Quantity'],"
               + datas
               + "        ]);"
               + "        var options = {"
               + "          title: 'Grades of " + filteredExamList.get(0).getProgramID()
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
        StringBuilder data = new StringBuilder(1024);

        // build data String
        for (int i = 0; i < GRADES.length; i++) {
            data.append("['")
                 .append(GRADES[i])
                 .append("', ")
                 .append(gradeDistrubution.get(GRADES[i]))
                 .append(']');
            if (i != GRADES.length - 1) {
                data.append(',');
            }
        }

        StringBuilder colors = new StringBuilder();
        colors.append("{ ");
        for(int i = 0; i < GRADES.length; i++){
            colors.append(i);
            colors.append(" : { color: ");
            colors.append(getHexColor(getColorForGrade(i)));
            colors.append(" }");
            if (i != GRADES.length - 1) {
                colors.append(',');
            }
        }
        colors.append(" }");

        Utils.log("landscape: " + landscape + ", dimension: " + chartSize);

        // build content String
        String html = "<html>"
                      + "  <head>"
                      + "    <script type=\"text/javascript\" src=\"https://www.gstatic.com/charts/loader.js\"></script>"
                      + "    <script type=\"text/javascript\">"
                      + "      google.charts.load(\"visualization\", \"1\", {packages:[\"corechart\"]});"
                      + "      google.charts.setOnLoadCallback(drawChart);"
                      + "      function drawChart() {"
                      + "        var data = new google.visualization.DataTable();"
                      + "        data.addColumn('string', '" + getString(R.string.grade) + "');"
                      + "        data.addColumn('number', '" + getString(R.string.number_of_exams) + "');"
                      + "        data.addRows([" + data + "]);"
                      + "        var options = {"
                      + "          title: '" + getString(R.string.grades_without_weight) + "' ,"
                      + (landscape ? "height" : "width") + " : " + (chartSize/2) + ","
                      + "          pieHole: 0.3 ,"
                      + "          pieSliceText: 'label',"
                      + "          slices: " + colors
                      + "        };"
                      + "        var chart = new google.visualization.PieChart(document.getElementById('chart_div'));"
                      + "        chart.draw(data, options);"
                      + "      }"
                      + "    </script>"
                      + "  </head>"
                      + "  <body>"
                      + "    <div id=\"chart_div\"></div>"
                      + "  </body>" + "</html>";
        Utils.log(html);
        return html;
    }

    private String getHexColor(int color){
        return "'#" + Integer.toHexString(getResources().getColor(color)).substring(2)  + "'";
    }
    private int getColorForGrade(int position){
        switch (position){
            case 0: return R.color.grade_1_0;
            case 1:
            case 2: return R.color.grade_1_3;
            case 3: return R.color.grade_1_7;
            case 4: return R.color.grade_2_0;
            case 5:
            case 6: return R.color.grade_2_3;
            case 7: return R.color.grade_3_0;
            case 8:
            case 9: return R.color.grade_3_3;
            case 10: return R.color.grade_3_7;
            case 11: return R.color.grade_4_0;
            case 12:
            case 13: return R.color.grade_4_3;
            case 14: return R.color.grade_4_7;
            case 15: return R.color.grade_5_0;
            default: return R.color.grade_default;
        }
    }

    /**
     * Calculates the average grade of the given exams
     *
     * @param filteredExamList List of exams
     * @return Average grade
     */
    Double calculateAverageGrade(List<Exam> filteredExamList) {
        double gradeSum = 0.0;

        for (Exam item : filteredExamList) {
            try {
                gradeSum += format.parse(item.getGrade()).doubleValue();
            } catch (ParseException e) {
                Utils.log(e);
            }
        }
        return gradeSum / filteredExamList.size();

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

        for (String GRADE : GRADES) {
            gradeDistribution.put(GRADE, 0);
        }

        for (Exam exam : filteredExamList) {
            // increment hash value
            int curCount = gradeDistribution.containsKey(exam.getGrade()) ? gradeDistribution
                    .get(exam.getGrade()) : 0;
            gradeDistribution.put(exam.getGrade(), curCount + 1);
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
        for (int i = 0; i < examList.getExams().size(); i++) {
            String item = examList.getExams().get(i).getProgramID();
            if (filters.indexOf(item) == -1) {
                filters.add(item);
            }
        }

        // init the spinner
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, filters);
        spFilter.setAdapter(spinnerArrayAdapter);
        spFilter.setSelection(lastChoice);

        // handle if program choice is changed
        spFilter.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

                String filter = spFilter.getItemAtPosition(arg2).toString();
                lastChoice = arg2;

                if (filter.equals(getString(R.string.all_programs))) {

                    // display all grades
                    lvGrades.setAdapter(new ExamListAdapter(
                            GradesActivity.this, examList.getExams()));
                    averageTx.setVisibility(View.GONE);
                    // convert exam list
                    List<Exam> convertedList = new ArrayList<>();
                    for (int i = 0; i < examList.getExams().size(); i++) {
                        Exam item = examList.getExams().get(i);
                        convertedList.add(item);
                    }

                    // build chart Content for corresponding list
                    columnChartContent = buildColumnChartContentString(convertedList);
                    pieChartContent = buildPieChartContentString(convertedList);
                    Utils.log(pieChartContent);

                } else {
                    // do filtering according to selected program
                    List<Exam> filteredExamList = new ArrayList<>();
                    for (Exam exam : examList.getExams()) {
                        if (exam.getProgramID().equals(filter)) {
                            filteredExamList.add(exam);
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
                lvGrades.setAdapter(new ExamListAdapter(GradesActivity.this, examList.getExams()));
            }
        });
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void showChart(boolean pie){
        chartWebView = findViewById(R.id.exam_chart);
        WebSettings webSettings = chartWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        //webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(false);
        chartWebView.requestFocusFromTouch();
        chartWebView.loadDataWithBaseURL("file:///android_asset/",
                                         (pie ? pieChartContent : columnChartContent),
                                         "text/html",
                                         "utf-8", null);
        if(chartVisible){
            chartWebView.setVisibility(View.VISIBLE);
        } else {
            chartWebView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        lvGrades = findViewById(R.id.lstGrades);
        spFilter = findViewById(R.id.spFilter);
        averageTx = findViewById(R.id.avgGrade);
        chartWebView = findViewById(R.id.exam_chart);
        mSwipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.color_primary, R.color.tum_A100, R.color.tum_A200);
        chartVisible = true;

        // if button exists we are in landscape mode
        landscape = findViewById(R.id.button_show_chart) != null;
        requestFetch();

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus){
        if(hasFocus){
            if(landscape){
                chartSize = chartWebView.getHeight();
            } else {
                chartSize = chartWebView.getWidth();
            }
        }
    }

    // for landscape
    public void showChart(View view){
        lvGrades.setVisibility(View.GONE);
        findViewById(R.id.button_show_chart).setVisibility(View.GONE);
        chartWebView.setVisibility(View.VISIBLE);
        findViewById(R.id.bottom_bar).setVisibility(View.VISIBLE);
    }

    public void showList(View view){
        lvGrades.setVisibility(View.VISIBLE);
        findViewById(R.id.button_show_chart).setVisibility(View.VISIBLE);
        chartWebView.setVisibility(View.VISIBLE);
        findViewById(R.id.bottom_bar).setVisibility(View.GONE);
    }

    // for portrait
    public void hideChartToggle(View view){
        if(chartVisible){
            chartWebView.setVisibility(View.GONE);
        } else {
            chartWebView.setVisibility(View.VISIBLE);
        }
        chartVisible = !chartVisible;
        view.setRotation(view.getRotation() + 180);
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
        Utils.log(examList.toString());

        // initialize the program choice spinner
        initSpinner();

        // Displays results in view
        lvGrades.setAdapter(new ExamListAdapter(this, examList.getExams()));

        showLoadingEnded();

        // enabling the Menu options after first fetch
        isFetched = true;
        pieChartContent = buildPieChartContentString(examList.getExams());
        columnChartContent = buildColumnChartContentString(examList.getExams());
        showChart(true);

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
                columnMenuItem.setVisible(false);
                pieMenuItem.setVisible(true);
                showChart(false);
                return true;
            } else if (i == R.id.pieChart) {
                columnMenuItem.setVisible(true);
                pieMenuItem.setVisible(false);
                showChart(true);
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

}
