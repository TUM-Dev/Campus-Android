package de.tum.in.tumcampusapp.component.tumui.tutionfees;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.Locale;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.api.tumonline.TUMOnlineClient;
import de.tum.in.tumcampusapp.component.other.generic.activity.ActivityForAccessingTumOnline;
import de.tum.in.tumcampusapp.component.tumui.tutionfees.model.Tuition;
import de.tum.in.tumcampusapp.component.tumui.tutionfees.model.TuitionList;
import retrofit2.Call;

/**
 * Activity to show the user's tuition ; based on grades.java / quick solution
 */
public class TuitionFeesActivity extends ActivityForAccessingTumOnline {

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
        ((TextView) findViewById(R.id.fees_aid)).setMovementMethod(LinkMovementMethod.getInstance());

        refreshData();
    }

    @Override
    public void onRefresh() {
        refreshData();
    }

    private void refreshData() {
        Call<TuitionList> apiCall = TUMOnlineClient
                .getInstance(this)
                .getTuitionFeesStatus();

        fetch(apiCall, this::displayTuition);
    }

    private void displayTuition(@NonNull TuitionList tuitionList) {
        Tuition tuition = tuitionList.getTuitions().get(0);

        String amountText = tuition.getOutstandingBalanceText();
        amountTextView.setText(amountText);

        DateTime deadline = tuitionList.getTuitions().get(0).getDueDate();
        deadlineTextView.setText(DateTimeFormat.longDate().print(deadline));

        String semester = tuition.getSemester().toUpperCase(Locale.getDefault());
        semesterTextView.setText(semester);

        if (tuition.getOutstandingBalance() == 0) {
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

        showLoadingEnded();
    }

}
