package de.tum.`in`.tumcampusapp.component.tumui.tutionfees

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.api.tumonline.CacheControl
import de.tum.`in`.tumcampusapp.component.other.generic.fragment.FragmentForAccessingTumOnline
import de.tum.`in`.tumcampusapp.component.tumui.tutionfees.model.TuitionList
import kotlinx.android.synthetic.main.fragment_tuition_fees.amountTextView
import kotlinx.android.synthetic.main.fragment_tuition_fees.deadlineTextView
import kotlinx.android.synthetic.main.fragment_tuition_fees.financialAidButton
import kotlinx.android.synthetic.main.fragment_tuition_fees.semesterTextView
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.util.Locale

class TuitionFeesFragment : FragmentForAccessingTumOnline<TuitionList>(
    R.layout.fragment_tuition_fees,
    R.string.tuition_fees
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        financialAidButton.setOnClickListener {
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

        val amountText = tuition.getAmountText(requireContext())
        amountTextView.text = amountText

        val deadline = tuition.deadline
        val formatter = DateTimeFormat.longDate().withLocale(Locale.getDefault())
        val formattedDeadline = formatter.print(deadline)
        deadlineTextView.text = getString(R.string.due_on_format_string, formattedDeadline)

        semesterTextView.text = tuition.semester

        if (tuition.isPaid) {
            amountTextView.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.sections_green))
        } else {
            // check if the deadline is less than a week from now
            val nextWeek = DateTime().plusWeeks(1)
            if (nextWeek.isAfter(deadline)) {
                amountTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.error))
            } else {
                amountTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            }
        }
    }

    companion object {
        @JvmStatic fun newInstance() = TuitionFeesFragment()
    }

}
