package de.tum.in.tumcampusapp.component.tumui.grades;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
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

import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.api.tumonline.TUMOnlineConst;
import de.tum.in.tumcampusapp.component.other.generic.activity.ActivityForAccessingTumOnline;
import de.tum.in.tumcampusapp.component.tumui.grades.model.Exam;
import de.tum.in.tumcampusapp.component.tumui.grades.model.ExamList;
import de.tum.in.tumcampusapp.utils.Utils;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * Activity to show the user's grades/exams passed.
 */
public class GradesActivity extends ActivityForAccessingTumOnline<ExamList> {

    private static final String SHOW_PIE_CHART = "showPieChart"; // show pie or bar chart after rotation
    private static final String SPINNER_POSITION = "spinnerPosition";

    private static final String[] GRADES = {"1,0", "1,3", "1,4", "1,7", "2,0", "2,3", "2,4", "2,7", "3,0", "3,3", "3,4", "3,7", "4,0", "4,3", "4,4", "4,7", "5,0"};
    private static final int[] GRADE_COLORS = {R.color.grade_1_0, R.color.grade_1_3, R.color.grade_1_4, R.color.grade_1_7,
                                               R.color.grade_2_0, R.color.grade_2_3, R.color.grade_2_4, R.color.grade_2_7,
                                               R.color.grade_3_0, R.color.grade_3_3, R.color.grade_3_4, R.color.grade_3_7,
                                               R.color.grade_4_0, R.color.grade_4_3, R.color.grade_4_4, R.color.grade_4_7,
                                               R.color.grade_5_0, R.color.grade_default};

    // exams data and list
    private List<Exam> examList;
    private String[] programIds;

    private StickyListHeadersListView lvGrades;
    private SwipeRefreshLayout swipeRefreshLayout;

    private Spinner spFilter;
    private int spinnerPosition;

    private boolean isFetched;

    // everything for the charts
    private MenuItem barMenuItem;
    private MenuItem pieMenuItem;

    private boolean chartVisible;
    private View chartView; // holds both the pieChart and the barChart
    private PieChart pieChart;
    private BarChart barChart;
    private boolean showBarChartAfterRotate;
    private int mMediumAnimationDuration;

    // average grade bar at the bottom
    private TextView tvAverageGrade;


    public GradesActivity() {
        super(TUMOnlineConst.Companion.getEXAMS(), R.layout.activity_grades);
    }

    private void showPieChart(List<Exam> exams) {

        // only animate if we are in the opposite state
        if(barChart.getVisibility() == View.VISIBLE && pieChart.getVisibility() == View.GONE){
            crossfade(barChart, pieChart);
        }

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
        pieChart.getLegend()
                .setWordWrapEnabled(true);
        pieChart.setDescription(null);
        pieChart.invalidate();
    }

    private void showBarChart(List<Exam> exams) {

        // only animate if we are in the opposite state
        if(pieChart.getVisibility() == View.VISIBLE && barChart.getVisibility() == View.GONE){
            crossfade(pieChart, barChart);
        }

        Map<String, Integer> gradeCount = calculateGradeDistribution(exams);

        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < GRADES.length; i++) {
            entries.add(new BarEntry(i, gradeCount.get(GRADES[i])));
        }

        BarDataSet set = new BarDataSet(entries, getString(R.string.grades_without_weight));
        set.setColors(GRADE_COLORS, this);

        BarData data = new BarData(set);

        barChart.setData(data);
        barChart.setFitBars(true);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setGranularity(1);
        xAxis.setValueFormatter((value, axis) -> GRADES[(int) value]);
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
                double grade = NumberFormat.getInstance()
                                           .parse(item.getGrade())
                                           .doubleValue();
                if (grade <= 4.0) {
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
            String item = examList.get(i)
                                  .getProgramID();
            if (!filters.contains(item)) {
                filters.add(item);
            }
        }
        programIds = filters.toArray(new String[]{});

        for (int i = 1; i < filters.size(); i++) {
            String programId = filters.get(i);
            filters.set(i, getString(R.string.study_program, programId));
        }

        // init the spinner
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this, R.layout.simple_spinner_item_actionbar, filters);
        spFilter.setAdapter(spinnerArrayAdapter);
        spFilter.setSelection(spinnerPosition);
        spFilter.setVisibility(View.VISIBLE);

        // handle if program choice is changed
        spFilter.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
                String filter = programIds[position];
                spinnerPosition = position;

                List<Exam> examsToShow;

                if (filter.equals(getString(R.string.all_programs))) {
                    examsToShow = examList;
                } else {
                    // do filtering according to selected program
                    List<Exam> filteredExamList = new ArrayList<>();
                    for (Exam exam : examList) {
                        if (exam.getProgramID()
                                .equals(filter)) {
                            filteredExamList.add(exam);
                        }
                    }
                    examsToShow = filteredExamList;
                }
                if (showBarChartAfterRotate) {
                    barMenuItem.setVisible(false);
                    pieMenuItem.setVisible(true);
                }
                showExams(examsToShow);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
    }

    private void showExams(List<Exam> exams) {
        lvGrades.setAdapter(new ExamListAdapter(
                GradesActivity.this, exams));
        if (chartVisible) {
            if (barMenuItem == null || barMenuItem.isVisible()) {
                showPieChart(exams);
            } else {
                showBarChart(exams);
            }
        }
        double averageGrade = Math.round(calculateAverageGrade(exams) * 100.0) / 100.0;
        tvAverageGrade.setText(String.format("%s: %s",
                                             getResources().getString(R.string.average_grade), averageGrade));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMediumAnimationDuration = getResources().getInteger(android.R.integer.config_mediumAnimTime);

        // chart
        barChart = findViewById(R.id.bar_chart);
        pieChart = findViewById(R.id.pie_chart);
        chartView = findViewById(R.id.charts);
        chartVisible = true;
        showBarChartAfterRotate = savedInstanceState != null && !savedInstanceState.getBoolean(SHOW_PIE_CHART, true);

        lvGrades = findViewById(R.id.lstGrades);
        spFilter = findViewById(R.id.spFilter);
        tvAverageGrade = findViewById(R.id.avgGrade);

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.color_primary, R.color.tum_A100, R.color.tum_A200);

        requestFetch();
    }

    @Override
    public void onSaveInstanceState(Bundle instanceState) {
        super.onSaveInstanceState(instanceState);
        instanceState.putBoolean(SHOW_PIE_CHART, barMenuItem.isVisible());
        instanceState.putInt(SPINNER_POSITION, spinnerPosition);
    }

    private void showChart(boolean show, boolean landscape) {
        if (show) {
            if (pieMenuItem.isVisible()) {
                barChart.setVisibility(View.VISIBLE);
            } else {
                pieChart.setVisibility(View.VISIBLE);
            }
            if (landscape) {
                swipeRefreshLayout.setVisibility(View.GONE);
            }
        } else {
            pieChart.setVisibility(View.GONE);
            barChart.setVisibility(View.GONE);
            swipeRefreshLayout.setVisibility(View.VISIBLE);

        }
        chartVisible = show;
    }

    // for landscape
    public void showChart(View view) {
        swipeRefreshLayout.setVisibility(View.GONE);
        view.setVisibility(View.GONE);
        findViewById(R.id.button_show_list).setVisibility(View.VISIBLE);
        showChart(true, true);
    }

    public void showList(View view) {
        swipeRefreshLayout.setVisibility(View.VISIBLE);
        findViewById(R.id.button_show_chart).setVisibility(View.VISIBLE);
        view.setVisibility(View.GONE);
        showChart(false, true);
    }

    // for portrait
    public void hideChartToggle(View view) {
        showChart(!chartVisible, false);
        view.setRotation(view.getRotation() + 180);
    }

    @Override
    protected void requestFetch() {
        super.requestFetch();
        swipeRefreshLayout.setRefreshing(true);
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
        showLoadingEnded();

        examList = rawResponse.getExams();

        // initialize the program choice spinner
        initSpinner();

        // Displays results in view
        lvGrades.setAdapter(new ExamListAdapter(this, examList));

        // enabling the Menu options after first fetch
        isFetched = true;
        showExams(examList);

        // update the action bar to display the enabled menu options
        this.invalidateOptionsMenu();
        swipeRefreshLayout.setRefreshing(false);
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
            if (chartVisible) {
                showBarChart(examList);
            }
            return true;
        } else if (i == R.id.pie_chart_menu) {
            barMenuItem.setVisible(true);
            pieMenuItem.setVisible(false);
            if (chartVisible) {
                showPieChart(examList);
            }
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * for switching out the different charts in a nice way
     * @param fadeout
     * @param fadein
     */
    public void crossfade(@NotNull View fadeout,@NotNull View fadein){
        // Set the content view to 0% opacity but visible, so that it is visible
        // (but fully transparent) during the animation.
        fadein.setAlpha(0f);
        fadein.setVisibility(View.VISIBLE);

        // Animate the content view to 100% opacity, and clear any animation
        // listener set on the view.
        fadein.animate()
                .alpha(1f)
                .setDuration(mMediumAnimationDuration)
                .setListener(null);

        // Animate the loading view to 0% opacity. After the animation ends,
        // set its visibility to GONE as an optimization step (it won't
        // participate in layout passes, etc.)
        fadeout.animate()
                .alpha(0f)
                .setDuration(mMediumAnimationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        fadeout.setVisibility(View.GONE);
                    }
                });

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
