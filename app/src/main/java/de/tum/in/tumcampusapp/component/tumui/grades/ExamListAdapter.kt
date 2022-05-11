package de.tum.`in`.tumcampusapp.component.tumui.grades

import android.content.Context
import android.database.DataSetObserver
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
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



        initUIDisplayElements(holder, exam)
        initUIEditElements(holder, exam);
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
        holder.editTextGradeWeights.setText(exam.weight.toString())
        holder.editTextGradeCredits.setText(exam.credits_new.toString())
        holder.checkBoxUseGradeForAverage.isChecked = exam.gradeUsedInAverage
        adaptUIToCheckboxStatus(exam.gradeUsedInAverage, holder, exam)

        if (localGradesFragment.getGlobalEdit()) {
            holder.editGradesContainer.visibility = View.GONE
            holder.gradeTextViewDeleteCustomGrade.visibility = View.GONE
        } else {
            holder.editGradesContainer.visibility = View.VISIBLE
            if (exam.manuallyAdded) {
                holder.gradeTextViewDeleteCustomGrade.visibility = View.VISIBLE
                holder.gradeTextViewDeleteCustomGrade.background.setTint(
                    ContextCompat.getColor(
                        context,
                        R.color.grade_default
                    )
                )
            }
        }

        holder.gradeTextViewDeleteCustomGrade.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
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

        })


        holder.buttonResetGradeParameters.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                exam.gradeUsedInAverage = false
                exam.weight = 1.0
                exam.credits_new = 6
                adaptUIToCheckboxStatus(false, holder, exam)
                holder.editTextGradeWeights.setText(exam.weight.toString())
                holder.editTextGradeCredits.setText(exam.credits_new.toString())
                // localGradesFragment.storeExamListInSharedPreferences()

            }

        })
        holder.editTextGradeWeights.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                exam.weight = s.toString().toDouble()
                //  localGradesFragment.storeExamListInSharedPreferences()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        holder.editTextGradeCredits.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                exam.credits_new = s.toString().toInt()
                // localGradesFragment.storeExamListInSharedPreferences()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        holder.checkBoxUseGradeForAverage.setOnCheckedChangeListener { buttonView, isChecked ->
            exam.gradeUsedInAverage = !isChecked
            adaptUIToCheckboxStatus(isChecked, holder, exam)
        }


        // holder.gradeTextViewDeleteCustomGrade.
    }

    private fun adaptUIToCheckboxStatus(
        gradeUsedInAverage: Boolean,
        holder: ViewHolder,
        exam: Exam
    ) {
        if (gradeUsedInAverage) {
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
        } else {
            holder.editTextGradeCredits.isEnabled = true;
            holder.editTextGradeWeights.isEnabled = true;
            val gradeColor = exam.getGradeColor(context)
            holder.gradeTextView.background.setTint(gradeColor)
            holder.gradeTextView.setTextColor(ContextCompat.getColor(context, R.color.white))
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

    override fun registerDataSetObserver(observer: DataSetObserver?) {
        super.registerDataSetObserver(observer)
    }
}
