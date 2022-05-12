package de.tum.`in`.tumcampusapp.component.tumui.grades

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.ArrayMap
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
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
import kotlinx.android.synthetic.main.fragment_grades.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
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

    private var globalEditOn = false
    private val exams = mutableListOf<Exam>()

    private val examSharedPreferences: String = "ExamList"
    private val scope = CoroutineScope(newSingleThreadContext("storetopreferences"))


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

        //loadExamListFromSharedPreferences();
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
        initSpinner(exams)
        showExams(exams)

        barMenuItem?.isEnabled = true
        pieMenuItem?.isEnabled = true

        isFetched = true
        requireActivity().invalidateOptionsMenu()

        storeGradedCourses(exams)
    }

    fun addExamToList(exam: Exam) {
        exams.add(exam)
        changeNumberOfExams()
    }

    fun deleteExamFromList(exam: Exam) {
        exams.remove(exam)
        changeNumberOfExams()
    }

    private fun changeNumberOfExams() {
        //(binding.gradesListView.adapter as ExamListAdapter).
        //(binding.gradesListView.adapter as ExamListAdapter).itemList=exams
        //(binding.gradesListView.adapter as ExamListAdapter).notifyDataSetChanged()


        binding.gradesListView.adapter = ExamListAdapter(requireContext(), exams, this)
        storeExamListInSharedPreferences()
    }

    /**
     * Adds all exams which are part f the new list to the existing exams list
     */
    private fun addAllNewItemsToExamList(examsDownloaded: MutableList<Exam>) {
        val examsTitles = exams.map { it.course }
        examsDownloaded.removeAll { examsTitles.contains(it.course) }
        //  exams.clear()
        //  storeExamListInSharedPreferences()


        if (!examsDownloaded.isEmpty()) {
            examsDownloaded.forEach { it.credits_new = 6; it.weight = 1.0; }
            exams.addAll(examsDownloaded)
            storeExamListInSharedPreferences()
        }
    }

    fun loadExamListFromSharedPreferences() {
        try {
            val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
            val dateTimeConverter = DateTimeConverter()
            val gson = GsonBuilder().registerTypeAdapter(DateTime::class.java, dateTimeConverter)
                .create()
            val listType = object : TypeToken<List<Exam>>() {}.type
            val jsonString = sharedPref.getString(examSharedPreferences, "")
            if (jsonString != null) {
                exams.clear()
                exams.addAll(gson.fromJson(jsonString, listType))
                return
            }
        } catch (e: Exception) {
            exams.clear()
        }
        // Exam list could no be loaded - will always e a list, some error occurred - clear to prevent any intermediate error states
    }

    private fun storeExamListInSharedPreferences() {

        scope.launch { storeExamListInSharedPreferencesThread() }

    }

    private fun storeExamListInSharedPreferencesThread() {
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

    fun storeGradedCourses(exams: List<Exam>) {
        val gradesStore = GradesStore(defaultSharedPreferences)
        val courses = exams.map { it.course }
        gradesStore.store(courses)
    }

    override fun onPause() {

        storeExamListInSharedPreferences()
        super.onPause()
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

        val set = BarDataSet(entries, getString(R.string.grades_without_weight)).apply {
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
            .filter { it.isPassed && !it.gradeUsedInAverage }
            .map { numberFormat.parse(it.grade).toDouble() * it.credits_new * it.weight }
        val combinedgradefactors = exams
            .filter { it.isPassed && !it.gradeUsedInAverage }
            .map { it.credits_new.toDouble() * it.weight }


        val gradeSum = grades.sum()
        val factorSum = combinedgradefactors.sum()
        if (factorSum<=0){
            return 0.0;
        }
        return gradeSum / factorSum.toDouble()
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
            requireContext(), R.layout.simple_spinner_item_actionbar, filters
        )

        with(binding) {
            filterSpinner.apply {
                adapter = spinnerArrayAdapter
                setSelection(spinnerPosition)
                visibility = View.VISIBLE
            }
        }
        binding.floatingbuttonAddExamGrade.setOnClickListener({ openAddGradeDialog() })


        binding.filterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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

    /**
     * Prompt the user to type in a name for the new chat room
     */
    private fun openAddGradeDialog() {
        // Set an EditText view to get user input
        val view = View.inflate(requireContext(), R.layout.dialog_add_grade_input, null)


        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Add a New Exam")
            .setMessage(
                "Grades are normally added automatically. In case special grades should be added, this can be done here. " +
                        "Manually added Exams can be deleted later on."
            )
            .setView(view)
            .setPositiveButton(R.string.create, null)
            .setNegativeButton(android.R.string.cancel, null)
            .create()
            .apply {
                window?.setBackgroundDrawableResource(R.drawable.rounded_corners_background)
            }

        dialog.setOnShowListener {
            val button: Button =
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            button.setOnClickListener {

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

                var weight: Double
                try {
                    weight = (weightView.text.toString()).toDouble()
                } catch (exception: Exception) {
                    weight = 1.0
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
                        "Wrong semester format: Too short. Correct two numbers for the year + W/S"
                } else if (!(semester.get(2).equals('W') || semester.get(2)
                        .equals('w') || semester.get(2).equals('S') || semester.get(2).equals('s'))
                ) {
                    changesRequired = true
                    semesterView.error =
                        "Wrong semester format: Term not specified. Correct two numbers for the year + W/S"
                }

                if (weight < 0.0) {             //weight sanitization
                    changesRequired = true
                    weightView.error = "Wrong weight format: Weights can not be negative."
                }

                val gradedouble = grade.replace(",", ".").toDouble()
                var gradeString = ""
                if (gradedouble <= 5.0 && gradedouble >= 1.0 || gradedouble == 0.0) {
                    if (gradedouble == 0.0) {
                        gradeString = "B"
                    } else {
                        gradeString = grade.toString().replace(".",",")
                    }
                } else {
                    changesRequired = true
                    gradeView.error =
                        "Wrong grade format: Grade must be between 1.0 and 5.0 or equal to 0."
                }


                if (title.length < 1) {             //title sanitization
                    changesRequired = true
                    titleView.error = "Insert a course title."
                }

                if (credits < 1) {             //title sanitization
                    changesRequired = true
                    creditsView.error = "Invalid amount of credits. Must be greater than 0."
                }


                if (!changesRequired) {
                    val typeConverter1 =
                        de.tum.`in`.tumcampusapp.api.tumonline.converters.DateTimeConverter()
                    val exam = Exam(
                        title,
                        typeConverter1.read(date),
                        examiner,
                        gradeString,
                        null,
                        "",
                        semester,
                        weight,
                        false,
                        credits,
                        true
                    )
                    addExamToList(exam)
                    dialog.dismiss()
                }
            }
        }

        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            .setTextColor(resources.getColor(R.color.text_primary))
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            .setTextColor(resources.getColor(R.color.text_primary))


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
            R.id.edit_grades_menu -> changeEditMode().run { true }
            else -> super.onOptionsItemSelected(item)

        }
    }

    /**
     * Toggles between the standard mode and the mode which allows to change grades.
     */
    private fun changeEditMode() {
        with(binding) {
            globalEditOn = !globalEditOn
            initUIVisibility()
        }
    }

    private fun initUIVisibility() {
        if (!globalEditOn) {
            frameLayoutAverageGrade?.visibility = View.GONE
            floatingbuttonAddExamGrade?.visibility = View.VISIBLE
        } else {
            storeExamListInSharedPreferences()
            showExams(exams)
            frameLayoutAverageGrade?.visibility = View.VISIBLE
            floatingbuttonAddExamGrade?.visibility = View.GONE
        }
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
        return globalEditOn
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

