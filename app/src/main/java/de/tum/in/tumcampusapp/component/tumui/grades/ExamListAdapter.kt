package de.tum.`in`.tumcampusapp.component.tumui.grades

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.generic.adapter.SimpleStickyListHeadersAdapter
import de.tum.`in`.tumcampusapp.component.tumui.grades.model.Exam
import kotlinx.android.synthetic.main.activity_grades_listview.view.*
import org.jetbrains.anko.db.INTEGER
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


        if (localGradesFragment.getGlobalEdit()) {
            holder.editGradesContainer.visibility = View.GONE
        } else {
            holder.editGradesContainer.visibility = View.VISIBLE

        }
       // localGradesFragment.storeExamListInSharedPreferences()
initUIElements(holder, exam);
        return view
    }

    private fun initUIElements(holder: ViewHolder, exam: Exam) {
        holder.editTextGradeWeights.setText(""+exam.weight)
        holder.editTextGradeCredits.setText(""+exam.credits_new)

        //todo hier noch auf die Werte Setzten, die das feld laut der Exam instanz hat
        holder.editTextGradeWeights.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                exam.weight= Integer.parseInt(s.toString())
                localGradesFragment.storeExamListInSharedPreferences()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        holder.editTextGradeCredits.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                exam.credits_new= Integer.parseInt(s.toString())
                localGradesFragment.storeExamListInSharedPreferences()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
    }

   /* fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
        this.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                exam
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                Log.d("Editable before textChanges",s.toString())
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.d("Editable on textChanges",s.toString())
            }
        })
    }*/

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
        var textViewCreditsosSubject: TextView =
            itemView.findViewById(R.id.textViewCreditsosSubject)
    }

    companion object {
        private val DATE_FORMAT = DateTimeFormat.mediumDate()
    }

}
