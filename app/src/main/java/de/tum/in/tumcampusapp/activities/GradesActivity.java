package de.tum.in.tumcampusapp.activities;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

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
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.models.tumo.Exam;
import de.tum.in.tumcampusapp.models.tumo.ExamList;
import de.tum.in.tumcampusapp.tumonline.TUMOnlineConst;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * Activity to show the user's grades/exams passed.
 */
public class GradesActivity extends ActivityForAccessingTumOnline<ExamList> {

    private static final String SHOW_PIE_CHART = "showPieChart"; // show pie or bar chart after rotation
    private static final String[] GRADES = {"1,0", "1,3", "1,4", "1,7", "2,0", "2,3", "2,4", "2,7", "3,0", "3,3", "3,4", "3,7", "4,0", "4,3", "4,4", "4,7", "5,0"};
    private static final int[] GRADE_COLORS = {R.color.grade_1_0, R.color.grade_1_3, R.color.grade_1_4, R.color.grade_1_7,
                                               R.color.grade_2_0, R.color.grade_2_3, R.color.grade_2_4, R.color.grade_2_7,
                                               R.color.grade_3_0, R.color.grade_3_3, R.color.grade_3_4, R.color.grade_3_7,
                                               R.color.grade_4_0, R.color.grade_4_3, R.color.grade_4_4, R.color.grade_4_7,
                                               R.color.grade_5_0, R.color.grade_default};

    private final NumberFormat format = NumberFormat.getInstance(Locale.FRANCE);
    private static int lastChoice;
    private TextView averageTx;
    private MenuItem barMenuItem;
    private MenuItem pieMenuItem;
    private List<Exam> examList;
    private StickyListHeadersListView lvGrades;
    private View listView;
    private PieChart pieChart;
    private BarChart barChart;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private String[] programIds;

    private boolean chartVisible;

    private Spinner spFilter;

    private boolean isFetched;
    private boolean showBarChartAfterRotate;

    public GradesActivity() {
        super(TUMOnlineConst.Companion.getEXAMS(), R.layout.activity_grades);
    }

    private void showPieChart(List<Exam> exams){
        barChart.setVisibility(View.GONE);
        pieChart.setVisibility(View.VISIBLE);

        Map<String, Integer> gradeCount = calculateGradeDistribution(exams);

        List<PieEntry> entries = new ArrayList<>();
        for (String GRADE : GRADES) {
            entries.add(new PieEntry(gradeCount.get(GRADE), GRADE));
        }

        PieDataSet set = new PieDataSet(entries, getString(R.string.grades_without_weight));
        set.setColors(GRADE_COLORS, this);
        set.setDrawValues(false);

        pieChart.setData(new PieData(set));
        pieChart.setDrawEntryLabels(false);
        pieChart.getLegend().setWordWrapEnabled(true);
        pieChart.setDescription(null);
        pieChart.invalidate();
    }

    private void showBarChart(List<Exam> exams){
        pieChart.setVisibility(View.GONE);
        barChart.setVisibility(View.VISIBLE);

        Map<String, Integer> gradeCount = calculateGradeDistribution(exams);

        List<BarEntry> entries = new ArrayList<>();
        for(int i = 0; i < GRADES.length; i++){
            entries.add(new BarEntry(i, gradeCount.get(GRADES[i])));
        }

        BarDataSet set = new BarDataSet(entries, getString(R.string.grades_without_weight));
        set.setColors(GRADE_COLORS, this);

        BarData data = new BarData(set);

        barChart.setData(data);
        barChart.setFitBars(true);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setGranularity(1);
        xAxis.setValueFormatter((value, axis) -> GRADES[(int)value]);
        barChart.setDescription(null);
        barChart.invalidate();
    }

    /**
     * Calculates the average grade of the given exams
     *
     * @param filteredExamList List of exams
     * @return Average grade
     */
    Double calculateAverageGrade(List<Exam> filteredExamList) {
        double gradeSum = 0.0;
        int grades = 0;

        for (Exam item : filteredExamList) {
            try {
                double grade = format.parse(item.getGrade()).doubleValue();
                if(grade <= 4.0){
                    gradeSum += grade;
                    grades++;
                }
            } catch (ParseException ignore) {
            }
        }
        return gradeSum / grades;

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
        for (int i = 0; i < examList.size(); i++) {
            String item = examList.get(i).getProgramID();
            if (!filters.contains(item)) {
                filters.add(item);
            }
        }
        programIds = filters.toArray(new String[]{});

        for(int i = 1; i < filters.size(); i++){
            String programId = filters.get(i);
            filters.set(i, getString(R.string.study_program, programId));
        }

        // init the spinner
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this, R.layout.simple_spinner_item_actionbar, filters);
        spFilter.setAdapter(spinnerArrayAdapter);
        spFilter.setSelection(lastChoice);
        spFilter.setVisibility(View.VISIBLE);

        // handle if program choice is changed
        spFilter.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
                String filter = programIds[position];
                lastChoice = position;

                List<Exam> examsToShow;

                if (filter.equals(getString(R.string.all_programs))) {
                    examsToShow = examList;
                } else {
                    // do filtering according to selected program
                    List<Exam> filteredExamList = new ArrayList<>();
                    for (Exam exam : examList) {
                        if (exam.getProgramID().equals(filter)) {
                            filteredExamList.add(exam);
                        }
                    }
                    examsToShow = filteredExamList;
                }
                if(showBarChartAfterRotate){
                    barMenuItem.setVisible(false);
                    pieMenuItem.setVisible(true);
                }
                showExams(examsToShow);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {}
        });
    }

    private void showExams(List<Exam> exams){
        lvGrades.setAdapter(new ExamListAdapter(
                GradesActivity.this, exams));
        if(chartVisible){
            if(barMenuItem.isVisible()){
                showPieChart(exams);
            } else {
                showBarChart(exams);
            }
        }
        double averageGrade = Math.round(calculateAverageGrade(exams) * 100.0) / 100.0;
        averageTx.setText(String.format("%s: %s",
                                        getResources().getString(R.string.average_grade), averageGrade));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        barChart = findViewById(R.id.bar_chart);
        pieChart = findViewById(R.id.pie_chart);
        lvGrades = findViewById(R.id.lstGrades);
        spFilter = findViewById(R.id.spFilter);
        averageTx = findViewById(R.id.avgGrade);
        mSwipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.color_primary, R.color.tum_A100, R.color.tum_A200);
        listView = mSwipeRefreshLayout;
        chartVisible = true;
        showBarChartAfterRotate = savedInstanceState != null && !savedInstanceState.getBoolean(SHOW_PIE_CHART, true);
        requestFetch();
    }

    @Override
    public void onSaveInstanceState(Bundle instanceState){
        super.onSaveInstanceState(instanceState);
        instanceState.putBoolean(SHOW_PIE_CHART, barMenuItem.isVisible());
    }

    private void showChart(boolean show, boolean landscape){
        if(show){
            if(pieMenuItem.isVisible()){
                barChart.setVisibility(View.VISIBLE);
            } else {
                pieChart.setVisibility(View.VISIBLE);
            }
            if(landscape){
                listView.setVisibility(View.GONE);
            }
        } else {
            pieChart.setVisibility(View.GONE);
            barChart.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);

        }
        chartVisible = show;
    }

    // for landscape
    public void showChart(View view){
        listView.setVisibility(View.GONE);
        view.setVisibility(View.GONE);
        findViewById(R.id.button_show_list).setVisibility(View.VISIBLE);
        showChart(true, true);
    }

    public void showList(View view){
        listView.setVisibility(View.VISIBLE);
        findViewById(R.id.button_show_chart).setVisibility(View.VISIBLE);
        view.setVisibility(View.GONE);
        showChart(false, true);
    }

    // for portrait
    public void hideChartToggle(View view){
        showChart(!chartVisible, false);
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
        barMenuItem = menu.findItem(R.id.bar_chart_menu);
        pieMenuItem = menu.findItem(R.id.pie_chart_menu);
        return true;
    }

    /**
     * Handle the response by de-serializing it into model entities.
     *
     * @param rawResponse Raw text response
     */
    @Override
    public void onFetch(ExamList rawResponse) {
        examList = rawResponse.getExams();
        Utils.log(examList.toString());

        // initialize the program choice spinner
        initSpinner();

        // Displays results in view
        lvGrades.setAdapter(new ExamListAdapter(this, examList));

        showLoadingEnded();

        // enabling the Menu options after first fetch
        isFetched = true;
        showExams(examList);

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
        int i = item.getItemId();
        if (i == R.id.bar_chart_menu) {
            barMenuItem.setVisible(false);
            pieMenuItem.setVisible(true);
            if(chartVisible){
                showBarChart(examList);
            }
            return true;
        } else if (i == R.id.pie_chart_menu) {
            barMenuItem.setVisible(true);
            pieMenuItem.setVisible(false);
            if(chartVisible){
                showPieChart(examList);
            }
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // enable Menu Items after fetching grades
        barMenuItem = menu.findItem(R.id.bar_chart_menu);
        barMenuItem.setEnabled(isFetched);
        pieMenuItem = menu.findItem(R.id.pie_chart_menu);
        pieMenuItem.setEnabled(isFetched);

        return super.onPrepareOptionsMenu(menu);
    }

}
