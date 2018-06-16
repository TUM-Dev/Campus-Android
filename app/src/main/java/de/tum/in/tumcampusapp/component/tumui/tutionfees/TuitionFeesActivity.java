package de.tum.in.tumcampusapp.component.tumui.tutionfees;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.Locale;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.api.tumonline.TUMOnlineConst;
import de.tum.in.tumcampusapp.component.other.generic.activity.ActivityForAccessingTumOnline;
import de.tum.in.tumcampusapp.component.tumui.tutionfees.model.Tuition;
import de.tum.in.tumcampusapp.component.tumui.tutionfees.model.TuitionList;

/**
 * Activity to show the user's tuition ; based on grades.java / quick solution
 */
public class TuitionFeesActivity extends ActivityForAccessingTumOnline<TuitionList> {

    private TextView amountTextView;
    private TextView deadlineTextView;
    private TextView semesterTextView;

    public TuitionFeesActivity() {
        super(TUMOnlineConst.TUITION_FEE_STATUS, R.layout.activity_tuitionfees);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        amountTextView = findViewById(R.id.soll);
        deadlineTextView = findViewById(R.id.deadline);
        semesterTextView = findViewById(R.id.semester);
        ((TextView) findViewById(R.id.fees_aid)).setMovementMethod(LinkMovementMethod.getInstance());

        requestFetch();
    }

    /**
     * Handle the response by de-serializing it into model entities.
     *
     * @param tuitionList TUMOnline response
     */
    @Override
    public void onFetch(TuitionList tuitionList) {
        Tuition tuition = tuitionList.getTuitions().get(0);

        String amountText = tuition.getOutstandingBalanceText();
        amountTextView.setText(amountText);

        DateTime deadline = tuitionList.getTuitions().get(0).getDueDate();
        deadlineTextView.setText(DateTimeFormat.longDate().print(deadline));
        semesterTextView.setText(tuitionList.getTuitions()
                .get(0)
                .getSemesterBez()
                .toUpperCase(Locale.getDefault()));

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
