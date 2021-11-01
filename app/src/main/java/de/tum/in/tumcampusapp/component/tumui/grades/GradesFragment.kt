package de.tum.`in`.tumcampusapp.component.tumui.grades

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.LayoutTransition
import android.graphics.Color
import android.graphics.drawable.Animatable
import android.os.Bundle
import android.util.ArrayMap
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LegendEntry
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.api.tumonline.CacheControl
import de.tum.`in`.tumcampusapp.component.other.generic.fragment.FragmentForAccessingTumOnline
import de.tum.`in`.tumcampusapp.component.tumui.grades.model.Exam
import de.tum.`in`.tumcampusapp.component.tumui.grades.model.ExamList
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Utils
import kotlinx.android.synthetic.main.fragment_grades.*
import org.jetbrains.anko.support.v4.defaultSharedPreferences
import java.text.NumberFormat
import java.util.*

class GradesFragment : FragmentForAccessingTumOnline<ExamList>(
        R.layout.fragment_grades,
        R.string.my_grades
) {

    private var spinnerPosition = 0
    private var isFetched = false

    private var barMenuItem: MenuItem? = null
    private var pieMenuItem: MenuItem? = null

    private var showBarChartAfterRotate = false

    private val grades: Array<String> by lazy {
        resources.getStringArray(R.array.grades)
    }

    private val animationDuration: Long by lazy {
        resources.getInteger(android.R.integer.config_mediumAnimTime).toLong()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        savedInstanceState?.let { state ->
            showBarChartAfterRotate = !state.getBoolean(KEY_SHOW_BAR_CHART, true)
            spinnerPosition = state.getInt(KEY_SPINNER_POSITION, 0)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (showBarChartAfterRotate) {
            barMenuItem?.isVisible = false
            pieMenuItem?.isVisible = true

            barChartView.visibility = View.VISIBLE
            pieChartView.visibility = View.GONE
        }

        showListButton?.setOnClickListener { toggleInLandscape() }
        showChartButton?.setOnClickListener { toggleInLandscape() }

        loadGrades(CacheControl.USE_CACHE)

        // Tracks whether the user has used the calendar module before. This is used in determining when to prompt for a
        // Google Play store review
        Utils.setSetting(requireContext(), Const.HAS_VISITED_GRADES, true)
    }

    override fun onRefresh() {
        loadGrades(CacheControl.BYPASS_CACHE)
    }

    private fun loadGrades(cacheControl: CacheControl) {
        val apiCall = apiClient.getGrades(cacheControl)
        fetch(apiCall)
    }

    override fun onDownloadSuccessful(response: ExamList) {
        initSpinner(response.exams)
        showExams(response.exams)

        barMenuItem?.isEnabled = true
        pieMenuItem?.isEnabled = true

        isFetched = true
        requireActivity().invalidateOptionsMenu()

        storeGradedCourses(response.exams)
    }

    private fun storeGradedCourses(exams: List<Exam>) {
        val gradesStore = GradesStore(defaultSharedPreferences)
        val courses = exams.map { it.course }
        gradesStore.store(courses)
    }

    /**
     * Displays the pie chart and its data set with the provided grade distribution.
     *
     * @param gradeDistribution An [ArrayMap] mapping grades to number of occurrences
     */
    private fun displayPieChart(gradeDistribution: ArrayMap<String, Int>) {
        val entries = grades.map { grade ->
            val count = gradeDistribution[grade] ?: 0
            PieEntry(count.toFloat(), grade)
        }

        val set = PieDataSet(entries, getString(R.string.grades_without_weight)).apply {
            setColors(GRADE_COLORS, requireContext())
            setDrawValues(false)
        }

        pieChartView.apply {
            data = PieData(set)
            setDrawEntryLabels(false)
            legend.isWordWrapEnabled = true
            description = null
            // the legend should not contain all possible grades but rather the most common ones
            legend.setEntries(legend.entries.filter {
                it.label != null && !it.label.contains(uncommonGradeRe)
            })
            legend.setCustom(legend.entries)
            setTouchEnabled(false)

            setHoleColor(Color.TRANSPARENT)
            legend.textColor = resources.getColor(R.color.text_primary) // TODO exchange deprecated function

            invalidate()
        }
    }

    /**
     * Displays the bar chart and its data set with the provided grade distribution.
     *
     * @param gradeDistribution An [ArrayMap] mapping grades to number of occurrence
     */
    private fun displayBarChart(gradeDistribution: ArrayMap<String, Int>) {
        val entries = grades.mapIndexed { index, grade ->
            val value = gradeDistribution[grade] ?: 0
            BarEntry(index.toFloat(), value.toFloat())
        }

        val set = BarDataSet(entries, getString(R.string.grades_without_weight)).apply {
            setColors(GRADE_COLORS, requireContext())
            valueTextColor = resources.getColor(R.color.text_primary)
        }

        barChartView.apply {
            data = BarData(set)
            setFitBars(true)

            // only label grades that are associated with at least one grade
            data.setValueFormatter(object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String? {
                    if (value > 0.0)
                        return value.toString()
                    return ""
                }
            })

            description = null
            setTouchEnabled(false)

            axisLeft.granularity = 1f
            axisRight.granularity = 1f

            description = null
            setTouchEnabled(false)
            legend.setCustom(
                    arrayOf(
                            LegendEntry(
                                    getString(R.string.grades_without_weight),
                                    Legend.LegendForm.SQUARE,
                                    10f,
                                    0f,
                                    null,
                                    ContextCompat.getColor(context, R.color.grade_default)
                            )
                    )
            )

            // TODO exchange deprecated function
            legend.textColor = resources.getColor(R.color.text_primary)
            xAxis.textColor = resources.getColor(R.color.text_primary)
            axisLeft.textColor = resources.getColor(R.color.text_primary)
            axisRight.textColor = resources.getColor(R.color.text_primary)

            invalidate()
        }
    }

    /**
     * Calculates the average grade of the provided exams.
     *
     * @param exams List of [Exam] objects
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
     * Calculates the grade distribution.
     *
     * @param exams List of [Exam] objects
     * @return An [ArrayMap] mapping grades to number of occurrence
     */
    private fun calculateGradeDistribution(exams: List<Exam>): ArrayMap<String, Int> {
        val gradeDistribution = ArrayMap<String, Int>()
        exams.forEach { exam ->
            // The grade distribution now takes grades with more than one decimal place into account as well
            var cleanGrade = exam.grade!!
            if (cleanGrade.contains(longGradeRe)) {
                cleanGrade = cleanGrade.subSequence(0, 3) as String
            }
            val count = gradeDistribution[cleanGrade] ?: 0
            gradeDistribution[cleanGrade] = count + 1
        }
        return gradeDistribution
    }

    /**
     * Initialize the spinner for choosing between the study programs. It determines all study
     * programs by iterating through the provided exams.
     *
     * @param exams List of [Exam] objects
     */
    private fun initSpinner(exams: List<Exam>) {
        val programIds = exams
                .map { it.programID }
                .distinct()
                .map { getString(R.string.study_program_format_string, it) }

        val filters = mutableListOf(getString(R.string.all_programs))
        filters.addAll(programIds)

        val spinnerArrayAdapter = ArrayAdapter(
                requireContext(), R.layout.simple_spinner_item_actionbar, filters)

        filterSpinner?.apply {
            adapter = spinnerArrayAdapter
            setSelection(spinnerPosition)
            visibility = View.VISIBLE
        }

        filterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
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
     * Displays all exams in the list view and chart view. If there are no exams, it displays a
     * placeholder instead.
     *
     * @param exams List of [Exam] object
     */
    private fun showExams(exams: List<Exam>) {
        if (exams.isEmpty()) {
            showError(R.string.no_grades)
            return
        }

        gradesListView.adapter = ExamListAdapter(requireContext(), exams)

        if (!isFetched) {
            // We hide the charts container in the beginning. Then, when we load data for the first
            // time, we make it visible. We don't do this on subsequent refreshes, as the user might
            // have decided to collapse the charts container and we don't want to revert that.
            chartsContainer.visibility = View.VISIBLE
        }

        calculateGradeDistribution(exams).apply {
            displayPieChart(this)
            displayBarChart(this)
        }

        val averageGrade = calculateAverageGrade(exams)
        averageGradeTextView.text = String.format("%s: %.2f", getString(R.string.average_grade), averageGrade)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(KEY_SHOW_BAR_CHART, barMenuItem?.isVisible ?: false)
        outState.putInt(KEY_SPINNER_POSITION, spinnerPosition)
        super.onSaveInstanceState(outState)
    }

    /**
     * Toggles between list view and chart view in landscape mode.
     */
    private fun toggleInLandscape() {
        val showChart = chartsContainer.visibility == View.GONE

        showListButton?.visibility = if (showChart) View.VISIBLE else View.GONE
        showChartButton?.visibility = if (showChart) View.GONE else View.VISIBLE

        val refreshLayout = swipeRefreshLayout ?: return

        if (chartsContainer.visibility == View.GONE) {
            crossFadeViews(refreshLayout, chartsContainer)
        } else {
            crossFadeViews(chartsContainer, refreshLayout)
        }
    }

    /**
     * Collapses or expands the chart above the list view. Only available in portrait mode. The
     * transition is animated via android:animateLayoutChanges in the layout file.
     */
    // TODO ???
    fun toggleChart(view: View) {
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
        (view as ImageView).apply {
            setImageResource(arrow)
            (drawable as Animatable).start()
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
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

        super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater?.inflate(R.menu.menu_activity_grades, menu)
        barMenuItem = menu?.findItem(R.id.bar_chart_menu)
        pieMenuItem = menu?.findItem(R.id.pie_chart_menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.bar_chart_menu,
            R.id.pie_chart_menu -> toggleChart().run { true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Toggles between the pie chart and the bar chart.
     */
    private fun toggleChart() {
        val showBarChart = barChartView.visibility == View.GONE

        barMenuItem?.isVisible = !showBarChart
        pieMenuItem?.isVisible = showBarChart

        if (chartsContainer.visibility == View.VISIBLE) {
            val fadeOut = if (showBarChart) pieChartView else barChartView
            val fadeIn = if (showBarChart) barChartView else pieChartView
            crossFadeViews(fadeOut, fadeIn)
        } else {
            // Switch layouts even though they are not visible. Once they are visible again,
            // the right chart will be displayed
            barChartView.visibility = if (showBarChart) View.VISIBLE else View.GONE
            pieChartView.visibility = if (!showBarChart) View.VISIBLE else View.GONE
        }
    }

    /**
     * Cross-fades two views.
     *
     * @param fadeOut The [View] that will fade out
     * @param fadeIn The [View] that will fade in
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

    companion object {
        private const val KEY_SHOW_BAR_CHART = "showPieChart"
        private const val KEY_SPINNER_POSITION = "spinnerPosition"

        fun newInstance() = GradesFragment()

        private val uncommonGradeRe = Regex("[1-5],[1245689][0-9]*")
        private val longGradeRe = Regex("[1-4],[0-9][0-9]+")

        private val GRADE_COLORS = intArrayOf(
                R.color.grade_1_0, R.color.grade_1_1, R.color.grade_1_2, R.color.grade_1_3,
                R.color.grade_1_4, R.color.grade_1_5, R.color.grade_1_6, R.color.grade_1_7,
                R.color.grade_1_8, R.color.grade_1_9,
                R.color.grade_2_0, R.color.grade_2_1, R.color.grade_2_2, R.color.grade_2_3,
                R.color.grade_2_4, R.color.grade_2_5, R.color.grade_2_6, R.color.grade_2_7,
                R.color.grade_2_8, R.color.grade_2_9,
                R.color.grade_3_0, R.color.grade_3_1, R.color.grade_3_2, R.color.grade_3_3,
                R.color.grade_3_4, R.color.grade_3_5, R.color.grade_3_6, R.color.grade_3_7,
                R.color.grade_3_8, R.color.grade_3_9,
                R.color.grade_4_0, R.color.grade_4_1, R.color.grade_4_2, R.color.grade_4_3,
                R.color.grade_4_4, R.color.grade_4_5, R.color.grade_4_6, R.color.grade_4_7,
                R.color.grade_4_8, R.color.grade_4_9,
                R.color.grade_5_0, R.color.grade_default
        )
    }
}
