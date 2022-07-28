package de.tum.`in`.tumcampusapp.component.tumui.grades

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.ArrayMap
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat.getColor
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LegendEntry
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.api.tumonline.CacheControl
import de.tum.`in`.tumcampusapp.component.other.generic.fragment.FragmentForAccessingTumOnline
import de.tum.`in`.tumcampusapp.component.tumui.grades.model.Exam
import de.tum.`in`.tumcampusapp.component.tumui.grades.model.ExamList
import de.tum.`in`.tumcampusapp.databinding.FragmentGradesBinding
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Utils
import org.jetbrains.anko.support.v4.defaultSharedPreferences
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import java.lang.reflect.Type
import java.text.NumberFormat
import java.util.*
import javax.inject.Inject


class GradesFragment : FragmentForAccessingTumOnline<ExamList>(
    R.layout.fragment_grades,
    R.string.my_grades
) {

    private var spinnerPosition = 0
    private var isFetched = false

    private var barMenuItem: MenuItem? = null
    private var pieMenuItem: MenuItem? = null

    private var showBarChartAfterRotate = false

    private var globalEditOFF = true
    private var adaptDiagramToWeights = true
    private val exams = mutableListOf<Exam>()

    private val examSharedPreferences: String = "ExamList"


    private val grades: Array<String> by lazy {
        resources.getStringArray(R.array.grades)
    }

    private val animationDuration: Long by lazy {
        resources.getInteger(android.R.integer.config_mediumAnimTime).toLong()
    }

    private val binding by viewBinding(FragmentGradesBinding::bind)

    override val swipeRefreshLayout get() = binding.swipeRefreshLayout
    override val layoutAllErrorsBinding get() = binding.layoutAllErrors

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

        with(binding) {
            if (showBarChartAfterRotate) {
                barMenuItem?.isVisible = false
                pieMenuItem?.isVisible = true

                barChartView.visibility = View.VISIBLE
                pieChartView.visibility = View.GONE
            }

            showListButton?.setOnClickListener { toggleInLandscape() }
            showChartButton?.setOnClickListener { toggleInLandscape() }
            initUIVisibility()
        }
        binding.floatingButtonAddExamGrade.setOnClickListener { openAddGradeDialog() }
        binding.checkboxUseDiagrams.setOnCheckedChangeListener { _, isChecked ->
            adaptDiagramToWeights = isChecked
        }


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
        val examsDownloaded: MutableList<Exam> = response.exams.orEmpty().toMutableList()
        loadExamListFromSharedPreferences()
        addAllNewItemsToExamList(examsDownloaded)
        initUIAfterDownloadingExams()

    }

    private fun initUIAfterDownloadingExams() {
        initSpinner(exams)
        showExams(exams)

        barMenuItem?.isEnabled = true
        pieMenuItem?.isEnabled = true

        isFetched = true
        requireActivity().invalidateOptionsMenu()

        storeGradedCourses(exams)
    }

    private fun addExamToList(exam: Exam) {
        exams.add(exam)
        changeNumberOfExams()
    }

    fun deleteExamFromList(exam: Exam) {
        exams.remove(exam)
        changeNumberOfExams()
    }

    private fun changeNumberOfExams() {
        binding.gradesListView.adapter = ExamListAdapter(requireContext(), exams, this)
        storeExamListInSharedPreferences()
    }

    /**
     * Adds all exams which are part f the new list to the existing exams list
     */
    private fun addAllNewItemsToExamList(examsDownloaded: MutableList<Exam>) {
        val examsTitles = exams.map { it.course }
        examsDownloaded.removeAll { examsTitles.contains(it.course) }


        if (examsDownloaded.isNotEmpty()) {
            examsDownloaded.forEach {
                it.credits_new = 6;it.weight = 1.0; it.gradeUsedInAverage = true
            }
            exams.addAll(examsDownloaded)
            storeExamListInSharedPreferences()
        }
    }

    private fun loadExamListFromSharedPreferences() {
        try {
            val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
            val dateTimeConverter = DateTimeConverter()
            val gson = GsonBuilder().registerTypeAdapter(DateTime::class.java, dateTimeConverter)
                .create()
            val listType = object : TypeToken<List<Exam>>() {}.type
            val jsonString = sharedPref.getString(examSharedPreferences, "")
            if (jsonString != null && jsonString != "[]") {
                exams.clear()
                exams.addAll(gson.fromJson(jsonString, listType))
                return
            }
        } catch (e: Exception) {
            exams.clear()
        }
    }


    fun storeExamListInSharedPreferences() {
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
        val dateTimeConverter = DateTimeConverter()
        val gson = GsonBuilder().registerTypeAdapter(DateTime::class.java, dateTimeConverter)
            .create()
        val jsonlist = gson.toJson(exams)
        with(sharedPref.edit()) {
            putString(examSharedPreferences, jsonlist)
            apply()
        }
    }


    /**
     * Gson serialiser/deserialiser for converting Joda [DateTime] objects.
     * Source: https://riptutorial.com/android/example/14799/adding-a-custom-converter-to-gson
     */
    class DateTimeConverter @Inject constructor() : JsonSerializer<DateTime?>,
        JsonDeserializer<DateTime?> {
        private val dateTimeFormatter: DateTimeFormatter =
            DateTimeFormat.forPattern("YYYY-MM-dd HH:mm")

        override fun serialize(
            src: DateTime?,
            typeOfSrc: Type?,
            context: JsonSerializationContext?
        ): JsonElement {
            return JsonPrimitive(dateTimeFormatter.print(src))
        }

        @Throws(JsonParseException::class)
        override fun deserialize(
            json: JsonElement,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): DateTime? {
            return if (json.asString == null || json.asString.isEmpty()) {
                null
            } else dateTimeFormatter.parseDateTime(json.asString)
        }

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


        var annotation = ""
        if (!adaptDiagramToWeights) {
            annotation = getString(R.string.grades_without_weight)
        }
        val set = PieDataSet(entries, annotation).apply {
            setColors(GRADE_COLORS, requireContext())
            setDrawValues(false)
        }

        with(binding) {
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
                legend.textColor = getColor(resources, R.color.text_primary, null)

                invalidate()
            }
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

        var annotation = ""
        if (!adaptDiagramToWeights) {
            annotation = getString(R.string.grades_without_weight)
        }
        val set = BarDataSet(entries, annotation).apply {
            setColors(GRADE_COLORS, requireContext())
            valueTextColor = resources.getColor(R.color.text_primary)
        }

        with(binding) {
            barChartView.apply {
                data = BarData(set)
                setFitBars(true)

                // only label grades that are associated with at least one grade
                data.setValueFormatter(object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
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

                legend.textColor = getColor(resources, R.color.text_primary, null)
                xAxis.textColor = getColor(resources, R.color.text_primary, null)
                axisLeft.textColor = getColor(resources, R.color.text_primary, null)
                axisRight.textColor = getColor(resources, R.color.text_primary, null)

                invalidate()
            }
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
            .filter { it.isPassed && it.gradeUsedInAverage }
            .map {
                (numberFormat.parse(it.grade.toString())?.toDouble()
                    ?: 1.0) * it.credits_new * it.weight
            }
        val factorSum = exams
            .filter { it.isPassed && it.gradeUsedInAverage }
            .map { it.credits_new.toDouble() * it.weight }.sum()


        val gradeSum = grades.sum()

        factorSum = max(factorSum , 0.0)
        return gradeSum / factorSum
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
            if (exam.gradeUsedInAverage) {
                var cleanGrade = exam.grade!!
                if (cleanGrade.contains(longGradeRe)) {
                    cleanGrade = cleanGrade.subSequence(0, 3) as String
                }
                val count = gradeDistribution[cleanGrade] ?: 0

                if (adaptDiagramToWeights) {
                    gradeDistribution[cleanGrade] = count + (exam.credits_new * exam.weight).toInt()
                } else {
                    gradeDistribution[cleanGrade] = count + 1
                }
            }
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

        if (programIds.size < 2) {
            binding.filterSpinner.visibility = View.GONE
        } else {
            val filters = mutableListOf(getString(R.string.all_programs))
            filters.addAll(programIds)

            val spinnerArrayAdapter = ArrayAdapter(
                requireContext(), R.layout.simple_spinner_item_actionbar, filters
            )

            with(binding) {
                filterSpinner.apply {
                    adapter = spinnerArrayAdapter
                    setSelection(spinnerPosition)
                    visibility = View.VISIBLE
                }
            }

            binding.filterSpinner.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
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
    }


    /**
     * Prompt the user to type in a custom exam grade.
     */
    private fun openAddGradeDialog() {
        val view = View.inflate(requireContext(), R.layout.dialog_add_grade_input, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.add_exam_dialog_title))
            .setMessage(
                getString(R.string.add_exam_dialog_message)
            )
            .setView(view)
            .create()
            .apply {
                window?.setBackgroundDrawableResource(R.drawable.rounded_corners_background)
            }

        dialog.show()
        dialog.findViewById<Button>(R.id.cancelDialogAddGrade)
            ?.setOnClickListener { dialog.dismiss() }
        dialog.findViewById<Button>(R.id.positiveDialogAddGrade)?.setOnClickListener {

            val titleView = view.findViewById<EditText>(R.id.editTextaddGradeCourseName)
            val gradeView = view.findViewById<EditText>(R.id.editTextAddGrade)
            val examinerView = view.findViewById<EditText>(R.id.editTextaddGradeExaminer)
            val weightView = view.findViewById<EditText>(R.id.editTextAddGradeWeight)
            val creditsView = view.findViewById<EditText>(R.id.editTextaddGradeCredits)
            val dateView = view.findViewById<EditText>(R.id.editTextAddGradeDate)
            val semesterView = view.findViewById<EditText>(R.id.editTextSemester)

            val title = titleView.text.toString()
            val grade = gradeView.text.toString()
            val examiner = examinerView.text.toString()
            val credits = Integer.parseInt(creditsView.text.toString())
            val date = dateView.text.toString()
            val semester = semesterView.text.toString()

            val weight: Double = try {
                (weightView.text.toString()).toDouble()
            } catch (exception: Exception) {
                1.0
            }
            titleView.error = null
            gradeView.error = null
            examinerView.error = null
            weightView.error = null
            creditsView.error = null
            dateView.error = null
            semesterView.error = null

            var changesRequired = false
            if (semester.length < 3) {                                                  //semester sanitization
                changesRequired = true
                semesterView.error =
                    getString(R.string.add_grade_error_wrong_semester)
            } else if (!(semester.get(2) == 'W' || semester[2] == 'w' || semester.get(2) == 'S' || semester.get(
                    2
                ) == 's')
            ) {
                changesRequired = true
                semesterView.error =
                    getString(R.string.add_grade_error_semester_no_ws)
            }

            if (weight < 0.0) { // weight sanitization
                changesRequired = true
                weightView.error = getString(R.string.add_grade_error_invalid_weight)
            }

            val gradedouble = grade.replace(",", ".").toDouble()
            var gradeString = ""
            if (gradedouble in 1.0..5.0 || gradedouble == 0.0) {
                gradeString = if (gradedouble == 0.0) {
                    "B"
                } else {
                    grade.replace(".", ",")
                }
            } else {
                changesRequired = true
                gradeView.error =
                    getString(R.string.add_grade_error_invalid_grade_format)
            }


            if (title.isEmpty()) { // title sanitization
                changesRequired = true
                titleView.error = getString(R.string.add_grade_missing_course_title)
            }

            if (credits < 1) { // title sanitization
                changesRequired = true
                creditsView.error =
                    getString(R.string.add_grade_error_invalid_weight_less_than_zero)
            }


            if (!changesRequired) {
                val typeConverter1 =
                    de.tum.`in`.tumcampusapp.api.tumonline.converters.DateTimeConverter()
                val exam = Exam(
                    title,
                    typeConverter1.read(date),
                    examiner,
                    gradeString,
                    "",
                    "",
                    semester,
                    weight,
                    true,
                    credits,
                    true
                )
                addExamToList(exam)
                dialog.dismiss()
            }
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

        binding.gradesListView.adapter = ExamListAdapter(requireContext(), exams, this)

        if (!isFetched) {
            // We hide the charts container in the beginning. Then, when we load data for the first
            // time, we make it visible. We don't do this on subsequent refreshes, as the user might
            // have decided to collapse the charts container and we don't want to revert that.
            binding.chartsContainer.visibility = View.VISIBLE
        }

        calculateGradeDistribution(exams).apply {
            displayPieChart(this)
            displayBarChart(this)
        }

        val averageGrade = calculateAverageGrade(exams)
        binding.averageGradeTextView.text =
            String.format("%s: %.2f", getString(R.string.average_grade), averageGrade)
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
        with(binding) {
            val showChart = chartsContainer.visibility == View.GONE

            showListButton?.visibility = if (showChart) View.VISIBLE else View.GONE
            showChartButton?.visibility = if (showChart) View.GONE else View.VISIBLE

            val refreshLayout = swipeRefreshLayout

            if (chartsContainer.visibility == View.GONE) {
                crossFadeViews(refreshLayout, chartsContainer)
            } else {
                crossFadeViews(chartsContainer, refreshLayout)
            }
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
        inflater.inflate(R.menu.menu_activity_grades, menu)
        barMenuItem = menu.findItem(R.id.bar_chart_menu)
        pieMenuItem = menu.findItem(R.id.pie_chart_menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.bar_chart_menu,
            R.id.pie_chart_menu -> toggleChart().run { true }
            R.id.edit_grades_menu -> changeEditMode(item).run { true };
            else -> super.onOptionsItemSelected(item)

        }
    }

    /**
     * Toggles between the standard mode and the mode which allows to change grades.
     */
    private fun changeEditMode(item: MenuItem) {
        globalEditOFF = !globalEditOFF
        if(globalEditOFF){
            item.setIcon(R.drawable.ic_outline_edit_24px);
        }else{
            item.setIcon(R.drawable.ic_baseline_save_24);
        }
        initUIVisibility()
    }

    private fun initUIVisibility() {
        val density = resources.displayMetrics.density
        val param = swipeRefreshLayout.layoutParams as ViewGroup.MarginLayoutParams
        if (!globalEditOFF) {
            binding.frameLayoutAverageGrade?.visibility = View.GONE
            binding.floatingButtonAddExamGrade.visibility = View.VISIBLE
            binding.chartsContainer.visibility = View.GONE
            binding.checkboxUseDiagrams.visibility = View.VISIBLE
            param.setMargins(0, ((32 * density + 0.5f).toInt()), 0, 0)
            binding.gradesListView.setPadding(0, 0, 0, 0)
        } else {
            showExams(exams)
            binding.frameLayoutAverageGrade?.visibility = View.VISIBLE
            binding.floatingButtonAddExamGrade.visibility = View.GONE
            binding.chartsContainer.visibility = View.VISIBLE
            binding.checkboxUseDiagrams.visibility = View.GONE
            param.setMargins(0, 0, 0, 0)
            binding.gradesListView.setPadding(0, ((256 * density + 0.5f).toInt()), 0, 0)
        }
        swipeRefreshLayout.layoutParams = param
    }

    /**
     * Toggles between the pie chart and the bar chart.
     */
    private fun toggleChart() {
        with(binding) {
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

    fun getGlobalEdit(): Boolean {
        return globalEditOFF
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
