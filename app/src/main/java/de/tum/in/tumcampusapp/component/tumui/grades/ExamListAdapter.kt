package de.tum.`in`.tumcampusapp.component.tumui.grades

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.generic.adapter.SimpleStickyListHeadersAdapter
import de.tum.`in`.tumcampusapp.component.tumui.grades.model.Exam
import org.joda.time.format.DateTimeFormat


/**
 * Custom UI adapter for a list of exams.
 */
class ExamListAdapter(context: Context, results: List<Exam>, gradesFragment: GradesFragment) :
    SimpleStickyListHeadersAdapter<Exam>(context, results.toMutableList()) {

    val localGradesFragment: GradesFragment = gradesFragment

    init {
        itemList.sort()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: ViewHolder
        val view: View

        if (convertView == null) {
            view = inflater.inflate(R.layout.activity_grades_listview, parent, false)
            holder = ViewHolder(view)
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as ViewHolder
        }

        val exam = itemList[position]


        initUIEditElements(holder, exam)
        initUIDisplayElements(holder, exam)
        return view
    }

    private fun initUIDisplayElements(holder: ViewHolder, exam: Exam) {
        holder.nameTextView.text = exam.course
        holder.gradeTextView.text = exam.grade

        val gradeColor = exam.getGradeColor(context)
        holder.gradeTextView.background.setTint(gradeColor)

        val date: String = if (exam.date == null) {
            context.getString(R.string.not_specified)
        } else {
            DATE_FORMAT.print(exam.date)
        }
        holder.examDateTextView.text =
            String.format("%s: %s", context.getString(R.string.date), date)

        holder.additionalInfoTextView.text = String.format(
            "%s: %s, %s: %s",
            context.getString(R.string.examiner), exam.examiner,
            context.getString(R.string.mode), exam.modus
        )
    }


    /**
     * Init the ui Elements to change the parameters of the grade
     */
    private fun initUIEditElements(holder: ViewHolder, exam: Exam) {
        if (localGradesFragment.getGlobalEdit()) {
            holder.editGradesContainer.visibility = View.GONE
            holder.gradeTextViewDeleteCustomGrade.visibility = View.GONE
        } else {
            holder.editGradesContainer.visibility = View.VISIBLE

            initListenerDeleteCustomGrade(exam, holder)
            initListenerEditTexts(exam, holder)
            initListenerResetGradeParameters(exam, holder)
            initCheckBoxUsedInAverage(exam, holder)
        }
    }


    /**
     * Adds a Clicklistener which will show confirmation dialog whether the exam should actually be deleted.
     */
    private fun initListenerDeleteCustomGrade(exam: Exam, holder: ViewHolder) {
        if (exam.manuallyAdded) {
            holder.gradeTextViewDeleteCustomGrade.visibility = View.VISIBLE
            adaptUIToCheckboxStatus(holder, exam)

            holder.gradeTextViewDeleteCustomGrade.setOnClickListener {
                val dialog = AlertDialog.Builder(localGradesFragment.requireContext())
                    .setTitle("Delete Exam")
                    .setMessage(
                        "Should this exam be irrevocably deleted"
                    )
                    .setPositiveButton("Delete") { dialogInterface, whichButton ->

                        localGradesFragment.deleteExamFromList(exam)
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .create()
                    .apply {
                        window?.setBackgroundDrawableResource(R.drawable.rounded_corners_background)
                    };
                dialog.show()
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(
                    localGradesFragment.getResources().getColor(R.color.text_primary)
                );
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                    localGradesFragment.getResources().getColor(R.color.text_primary)
                );
            }
        } else {
            holder.gradeTextViewDeleteCustomGrade.visibility = View.GONE
        }

    }


    /**
     * Adds on Focus change listeners which store the value to the exam object if and only if the
     * user finished editing the exam.
     */
    private fun initListenerEditTexts(exam: Exam, holder: ViewHolder) {
        holder.editTextGradeWeights.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val helper = holder.editTextGradeWeights.text.toString().toDouble()
                if (exam.weight != helper) {
                    exam.weight = helper;
                }
            }
        }

        holder.editTextGradeCredits.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val helper = holder.editTextGradeCredits.text.toString().toInt()
                if (exam.credits_new != helper) {
                    exam.credits_new = helper;
                }
            }
        }
        holder.editTextGradeWeights.setText(exam.weight.toString())
        holder.editTextGradeCredits.setText(exam.credits_new.toString())
    }


    /**
     * Adds a ClickListener to reset one exam to the default values and adapts the UI accordingly
     */
    private fun initListenerResetGradeParameters(exam: Exam, holder: ViewHolder) {
        holder.buttonResetGradeParameters.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                exam.gradeUsedInAverage = true
                adaptUIToCheckboxStatus(holder, exam)

                exam.weight = 1.0
                exam.credits_new = 6
                holder.editTextGradeWeights.setText(exam.weight.toString())
                holder.editTextGradeCredits.setText(exam.credits_new.toString())
            }

        })
    }

    /**
     * Initialises the state of the checkbox and adapts the ui accordingly
     */
    private fun initCheckBoxUsedInAverage(exam: Exam, holder: ViewHolder) {
        holder.checkBoxUseGradeForAverage.isChecked = exam.gradeUsedInAverage
        adaptUIToCheckboxStatus(holder, exam)
        holder.checkBoxUseGradeForAverage.setOnCheckedChangeListener { _, isChecked ->
            exam.gradeUsedInAverage = !isChecked
            adaptUIToCheckboxStatus(holder, exam)
        }
    }

    /**
     * Enables/disables Edittexts, and adapts the color of the grade bar on the right side
     */
    private fun adaptUIToCheckboxStatus(
        holder: ViewHolder,
        exam: Exam
    ) {
        if (exam.gradeUsedInAverage) {
            holder.editTextGradeCredits.isEnabled = true;
            holder.editTextGradeWeights.isEnabled = true;
            val gradeColor = exam.getGradeColor(context)
            holder.gradeTextView.background.setTint(gradeColor)
            holder.gradeTextView.setTextColor(ContextCompat.getColor(context, R.color.white))
        } else {
            holder.editTextGradeCredits.isEnabled = false;
            holder.editTextGradeWeights.isEnabled = false;
            holder.gradeTextView.background.setTint(
                ContextCompat.getColor(
                    context,
                    R.color.transparent
                )
            )
            holder.gradeTextView.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.grade_default
                )
            )
        }
    }


    override fun generateHeaderName(item: Exam): String {
        val headerText = super.generateHeaderName(item)
        val year = Integer.parseInt(headerText.substring(0, 2))
        return if (headerText[2] == 'W') {
            context.getString(R.string.winter_semester, year, year + 1)
        } else {
            context.getString(R.string.summer_semester, year)
        }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    internal class ViewHolder(itemView: View) {
        var nameTextView: TextView = itemView.findViewById(R.id.courseNameTextView)
        var gradeTextView: TextView = itemView.findViewById(R.id.gradeTextView)
        var examDateTextView: TextView = itemView.findViewById(R.id.examDateTextView)
        var additionalInfoTextView: TextView = itemView.findViewById(R.id.additionalInfoTextView)

        var editTextGradeWeights: EditText = itemView.findViewById(R.id.editTextGradeWeight)
        var editTextGradeCredits: EditText = itemView.findViewById(R.id.editTextCreditsofSubject)
        var editGradesContainer: LinearLayout = itemView.findViewById(R.id.editGradesContainer)
        var checkBoxUseGradeForAverage: CheckBox =
            itemView.findViewById(R.id.checkBoxUseGradeForAverage)
        val buttonResetGradeParameters: Button =
            itemView.findViewById(R.id.buttonResetGradeParameters)
        val gradeTextViewDeleteCustomGrade: ImageView =
            itemView.findViewById(R.id.gradeTextViewDeleteCustomGrade)
    }

    companion object {
        private val DATE_FORMAT = DateTimeFormat.mediumDate()
    }
}
