package de.tum.in.tumcampusapp.component.tumui.tutionfees;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.button.MaterialButton;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

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

        amountTextView = findViewById(R.id.amountTextView);
        deadlineTextView = findViewById(R.id.deadlineTextView);
        semesterTextView = findViewById(R.id.semesterTextView);

        MaterialButton button = findViewById(R.id.financialAidButton);
        button.setOnClickListener(v -> {
            String url = getString(R.string.student_financial_aid_link);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        });

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
        DateTimeFormatter formatter = DateTimeFormat.longDate().withLocale(Locale.getDefault());
        String formattedDeadline = formatter.print(deadline);
        deadlineTextView.setText(getString(R.string.due_on_format_string, formattedDeadline));

        String semester = tuition.getSemester();
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
