package de.tum.`in`.tumcampusapp.component.tumui.tutionfees

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.google.android.material.button.MaterialButton
import androidx.core.content.ContextCompat
import android.widget.TextView
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.api.tumonline.CacheControl
import de.tum.`in`.tumcampusapp.component.other.generic.activity.ActivityForAccessingTumOnline
import de.tum.`in`.tumcampusapp.component.tumui.tutionfees.model.TuitionList
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.util.*

/**
 * Activity to show the user's tuition fees status
 */
class TuitionFeesActivity : ActivityForAccessingTumOnline<TuitionList>(R.layout.activity_tuitionfees) {

    private val amountTextView by lazy { findViewById<TextView>(R.id.amountTextView) }
    private val deadlineTextView by lazy { findViewById<TextView>(R.id.deadlineTextView) }
    private val semesterTextView by lazy { findViewById<TextView>(R.id.semesterTextView) }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val button = findViewById<MaterialButton>(R.id.financialAidButton)
        button.setOnClickListener {
            val url = getString(R.string.student_financial_aid_link)
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }

        refreshData(CacheControl.USE_CACHE)
    }

    override fun onRefresh() {
        refreshData(CacheControl.BYPASS_CACHE)
    }

    private fun refreshData(cacheControl: CacheControl) {
        val apiCall = apiClient.getTuitionFeesStatus(cacheControl)
        fetch(apiCall)
    }

    override fun onDownloadSuccessful(response: TuitionList) {
        val tuition = response.tuitions.first()

        val amountText = tuition.getAmountText(this)
        amountTextView.text = amountText

        val deadline = tuition.deadline
        val formatter = DateTimeFormat.longDate().withLocale(Locale.getDefault())
        val formattedDeadline = formatter.print(deadline)
        deadlineTextView.text = getString(R.string.due_on_format_string, formattedDeadline)

        semesterTextView.text = tuition.semester

        if (tuition.isPaid) {
            amountTextView.setTextColor(ContextCompat.getColor(this, R.color.sections_green))
        } else {
            // check if the deadline is less than a week from now
            val nextWeek = DateTime().plusWeeks(1)
            if (nextWeek.isAfter(deadline)) {
                amountTextView.setTextColor(ContextCompat.getColor(this, R.color.error))
            } else {
                amountTextView.setTextColor(ContextCompat.getColor(this, R.color.black))
            }
        }
    }

}
