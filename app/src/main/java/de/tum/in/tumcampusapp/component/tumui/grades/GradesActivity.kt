package de.tum.`in`.tumcampusapp.component.tumui.grades

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.LayoutTransition
import android.graphics.drawable.Animatable
import android.os.Bundle
import android.util.ArrayMap
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import com.github.mikephil.charting.data.*
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.api.tumonline.CacheControl
import de.tum.`in`.tumcampusapp.component.other.generic.activity.ActivityForAccessingTumOnline
import de.tum.`in`.tumcampusapp.component.tumui.grades.model.Exam
import de.tum.`in`.tumcampusapp.component.tumui.grades.model.ExamList
import kotlinx.android.synthetic.main.activity_grades.*
import java.text.NumberFormat
import java.util.*

/**
 * Activity to show the user's grades/exams passed.
 */
class GradesActivity : ActivityForAccessingTumOnline<ExamList>(R.layout.activity_grades) {

    // exams data and list
    private var exams: List<Exam>? = null
    private var programIds: List<String>? = null

    private var spinnerPosition = 0

    private var isFetched = false

    // everything for the charts
    private var barMenuItem: MenuItem? = null
    private var pieMenuItem: MenuItem? = null

    private var showBarChartAfterRotate = false

    private val grades: Array<String> by lazy {
        resources.getStringArray(R.array.grades)
    }

    private val animationDuration: Long by lazy {
        resources.getInteger(android.R.integer.config_mediumAnimTime).toLong()
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        savedInstanceState?.let { state ->
            showBarChartAfterRotate = !state.getBoolean(KEY_SHOW_PIE_CHART, true)
        }

        if (showBarChartAfterRotate) {
            barMenuItem?.isVisible = false
            pieMenuItem?.isVisible = true

            barChartView.visibility = View.VISIBLE
            pieChartView.visibility = View.GONE
        }

        isFetched = false
        loadGrades(CacheControl.USE_CACHE)
    }

    override fun onRefresh() {
        loadGrades(CacheControl.BYPASS_CACHE)
    }

    private fun initPieChart(gradeDistribution: ArrayMap<String, Int>) {
        val entries = grades.map { grade ->
            val count = gradeDistribution[grade] ?: 0
            PieEntry(count.toFloat(), grade)
        }

        val set = PieDataSet(entries, getString(R.string.grades_without_weight)).apply {
            setColors(GRADE_COLORS, this@GradesActivity)
            setDrawValues(false)
        }

        pieChartView.apply {
            data = PieData(set)
            setDrawEntryLabels(false)
            legend.isWordWrapEnabled = true
            description = null
            invalidate()
        }
    }

    private fun initBarChart(gradeDistribution: ArrayMap<String, Int>) {
        val entries = grades.mapIndexed { index, grade ->
            val value = gradeDistribution[grade] ?: 0
            BarEntry(index.toFloat(), value.toFloat())
        }

        val set = BarDataSet(entries, getString(R.string.grades_without_weight)).apply {
            setColors(GRADE_COLORS, this@GradesActivity)
        }

        barChartView.apply {
            data = BarData(set)
            setFitBars(true)

            xAxis.apply {
                granularity = 1f
                setValueFormatter { value, _ -> grades[value.toInt()] }
            }

            description = null
            invalidate()
        }
    }

    /**
     * Calculates the average grade of the given exams
     *
     * @param exams List of exams
     * @return Average grade
     */
    private fun calculateAverageGrade(exams: List<Exam>): Double {
        val numberFormat = NumberFormat.getInstance(Locale.GERMAN)
        val grades = exams
                .filter { it.isPassed }
                .map { numberFormat.parse(it.grade).toDouble() }

        val gradeSum = grades.sum()
        return gradeSum / grades.size.toDouble()
    }

    /**
     * Calculates grade distribution
     *
     * @param exams List of exams
     * @return HashMap with grade to grade count mapping
     */
    private fun calculateGradeDistribution(exams: List<Exam>): ArrayMap<String, Int> {
        val gradeDistribution = ArrayMap<String, Int>()
        exams.forEach { exam ->
            val count = gradeDistribution[exam.grade] ?: 0
            gradeDistribution[exam.grade] = count + 1
        }
        return gradeDistribution
    }

    /**
     * Initialize the spinner for choosing between the study programs.
     */
    private fun initSpinner(exams: List<Exam>) {
        // Set Spinner data
        val filters = arrayListOf<String>(getString(R.string.all_programs))

        // Get all program IDs from the results
        programIds = exams
                .map { it.programID }
                .distinct()
                .map { getString(R.string.study_program_format_string, it) }
                .apply { filters.addAll(this) }

        // Init the spinner
        val spinnerArrayAdapter = ArrayAdapter(
                this, R.layout.simple_spinner_item_actionbar, filters)

        filterSpinner?.apply {
            adapter = spinnerArrayAdapter
            setSelection(spinnerPosition)
            visibility = View.VISIBLE
        }

        // Handle if program choice is changed
        filterSpinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val filter = filters[position]
                spinnerPosition = position

                val examsToShow = when (position) {
                    0 -> exams
                    else -> exams.filter { filter.contains(it.programID) }
                }

                showExams(examsToShow)
            }

            override fun onNothingSelected(parent: AdapterView<*>) = Unit
        }
    }

    /**
     * updates/inits all components to show the given exams
     * @param exams
     */
    private fun showExams(exams: List<Exam>) {
        if (exams.isEmpty()) {
            showError(R.string.no_grades)
            return
        }

        gradesListView.adapter = ExamListAdapter(this@GradesActivity, exams)

        // Update charts
        chartsContainer.visibility = View.VISIBLE
        calculateGradeDistribution(exams).apply {
            initPieChart(this)
            initBarChart(this)
        }

        val averageGrade = calculateAverageGrade(exams)
        averageGradeTextView.text = String.format("%s: %.2f", getString(R.string.average_grade), averageGrade)
    }

    public override fun onSaveInstanceState(instanceState: Bundle) {
        super.onSaveInstanceState(instanceState)
        instanceState.putBoolean(KEY_SHOW_PIE_CHART, barMenuItem?.isVisible ?: false)
        instanceState.putInt(KEY_SPINNER_POSITION, spinnerPosition)
    }

    /**
     * animated due to android:animateLayoutChanges="true" in xml file
     */
    private fun toggleChartVisibility() {
        val transition = LayoutTransition()

        val showCharts = chartsContainer.visibility == View.GONE
        chartsContainer.visibility = if (showCharts) View.VISIBLE else View.GONE

        val arrow = if (showCharts) R.drawable.ic_arrow_anim_up else R.drawable.ic_arrow_anim_down

        if (showCharts) {
            transition.addChild(gradesLayout, chartsContainer)
        } else {
            transition.removeChild(gradesLayout, chartsContainer)
        }

        // Animate arrow
        chartVisibilityToggle.setImageResource(arrow)
        (chartVisibilityToggle.drawable as Animatable).start()
    }

    private fun toggleChartVisibilityLand() {
        if (chartsContainer.visibility == View.GONE) {
            // Show charts and hide list
            crossFadeViews(swipeRefreshLayout, chartsContainer)
        } else {
            // Hide charts and show list
            crossFadeViews(chartsContainer, swipeRefreshLayout)
        }
    }

    // for landscape
    fun toggleInLandscape(view: View) {
        val showChart = chartsContainer.visibility == View.GONE

        swipeRefreshLayout.visibility = if (showChart) View.GONE else View.VISIBLE
        showListButton.visibility = if (showChart) View.VISIBLE else View.GONE
        showChartButton.visibility = if (showChart) View.GONE else View.VISIBLE

        toggleChartVisibilityLand()
    }

    // for portrait
    fun hideChartToggle(view: View) {
        toggleChartVisibility()
    }

    private fun loadGrades(cacheControl: CacheControl) {
        val apiCall = apiClient.getGrades(cacheControl)
        fetch(apiCall)
    }

    override fun onDownloadSuccessful(response: ExamList) {
        exams = response.exams.apply {
            initSpinner(this)
            showExams(this)
        }

        // Enable the menu options after first successful fetch
        isFetched = true

        barMenuItem?.isEnabled = true
        pieMenuItem?.isEnabled = true

        // Update the action bar to display the enabled menu options
        invalidateOptionsMenu()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu_activity_grades, menu)
        barMenuItem = menu.findItem(R.id.bar_chart_menu)
        pieMenuItem = menu.findItem(R.id.pie_chart_menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.bar_chart_menu -> showBarChart().run { true }
            R.id.pie_chart_menu -> showPieChart().run { true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showBarChart() {
        barMenuItem?.isVisible = false
        pieMenuItem?.isVisible = true

        if (chartsContainer.visibility == View.VISIBLE) {
            crossFadeViews(pieChartView, barChartView)
        } else {
            // switch layouts even though they are not visible
            // --> when they are visible again the right chart will be displayed
            pieChartView.visibility = View.GONE
            barChartView.visibility = View.VISIBLE
        }
    }

    private fun showPieChart() {
        barMenuItem?.isVisible = true
        pieMenuItem?.isVisible = false

        if (chartsContainer.visibility == View.VISIBLE) {
            crossFadeViews(barChartView, pieChartView)
        } else {
            pieChartView.visibility = View.VISIBLE
            barChartView.visibility = View.GONE
        }
    }

    /**
     * switching out two layouts by fading one in and the other one out (at the same time)
     * @param fadeOut
     * @param fadeIn
     */
    private fun crossFadeViews(fadeOut: View, fadeIn: View) {
        // Set the content view to 0% opacity but visible, so that it is visible
        // (but fully transparent) during the animation.
        fadeIn.alpha = 0f
        fadeIn.visibility = View.VISIBLE

        // Animate the content view to 100% opacity, and clear any animation
        // listener set on the view.
        fadeIn.animate()
                .alpha(1f)
                .setDuration(animationDuration)
                .setListener(null)

        // Animate the loading view to 0% opacity. After the animation ends,
        // set its visibility to GONE as an optimization step (it won't
        // participate in layout passes, etc.)
        fadeOut.animate()
                .alpha(0f)
                .setDuration(animationDuration)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        fadeOut.visibility = View.GONE
                    }
                })
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        // enable Menu Items after fetching grades
        barMenuItem = menu.findItem(R.id.bar_chart_menu).apply {
            isEnabled = isFetched
        }

        pieMenuItem = menu.findItem(R.id.pie_chart_menu).apply {
            isEnabled = isFetched
        }

        if (showBarChartAfterRotate) {
            barMenuItem?.isVisible = false
            pieMenuItem?.isVisible = true
        }

        return super.onPrepareOptionsMenu(menu)
    }

    companion object {
        private const val KEY_SHOW_PIE_CHART = "showPieChart" // show pie or bar chart after rotation
        private const val KEY_SPINNER_POSITION = "spinnerPosition"

        private val GRADE_COLORS = intArrayOf(
                R.color.grade_1_0, R.color.grade_1_3, R.color.grade_1_4, R.color.grade_1_7,
                R.color.grade_2_0, R.color.grade_2_3, R.color.grade_2_4, R.color.grade_2_7,
                R.color.grade_3_0, R.color.grade_3_3, R.color.grade_3_4, R.color.grade_3_7,
                R.color.grade_4_0, R.color.grade_4_3, R.color.grade_4_4, R.color.grade_4_7,
                R.color.grade_5_0, R.color.grade_default
        )
    }

}
