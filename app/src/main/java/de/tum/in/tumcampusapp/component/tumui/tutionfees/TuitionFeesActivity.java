package de.tum.in.tumcampusapp.component.tumui.tutionfees;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.Locale;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.api.tumonline.CacheControl;
import de.tum.in.tumcampusapp.component.other.generic.activity.ActivityForAccessingTumOnline;
import de.tum.in.tumcampusapp.component.tumui.tutionfees.model.Tuition;
import de.tum.in.tumcampusapp.component.tumui.tutionfees.model.TuitionList;
import retrofit2.Call;

/**
 * Activity to show the user's tuition fees status
 */
public class TuitionFeesActivity extends ActivityForAccessingTumOnline<TuitionList> {

    private TextView amountTextView;
    private TextView deadlineTextView;
    private TextView semesterTextView;

    public TuitionFeesActivity() {
        super(R.layout.activity_tuitionfees);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        amountTextView = findViewById(R.id.soll);
        deadlineTextView = findViewById(R.id.deadline);
        semesterTextView = findViewById(R.id.semester);

        // Set the text in the information box and make the link clickable
        TextView informationTextView = findViewById(R.id.fees_aid);
        Spanned information = Html.fromHtml(getString(R.string.tuition_fee_more));
        informationTextView.setText(information);
        informationTextView.setMovementMethod(LinkMovementMethod.getInstance());

        refreshData(CacheControl.USE_CACHE);
    }

    @Override
    public void onRefresh() {
        refreshData(CacheControl.BYPASS_CACHE);
    }

    private void refreshData(CacheControl cacheControl) {
        Call<TuitionList> apiCall = apiClient.getTuitionFeesStatus(cacheControl);
        fetch(apiCall);
    }

    @Override
    protected void onDownloadSuccessful(@NonNull TuitionList response) {
        Tuition tuition = response.getTuitions().get(0);

        String amountText = tuition.getAmountText(this);
        amountTextView.setText(amountText);

        DateTime deadline = tuition.getDeadline();
        deadlineTextView.setText(DateTimeFormat.longDate().print(deadline));

        String semester = tuition.getSemester().toUpperCase(Locale.getDefault());
        semesterTextView.setText(semester);

        if (tuition.isPaid()) {
            amountTextView.setTextColor(getResources().getColor(R.color.sections_green));
        } else {
            // check if the deadline is less than a week from now
            DateTime nextWeek = new DateTime().plusWeeks(1);
            if (nextWeek.isAfter(deadline)) {
                amountTextView.setTextColor(getResources().getColor(R.color.error));
            } else {
                amountTextView.setTextColor(getResources().getColor(R.color.black));
            }
        }
    }

}
