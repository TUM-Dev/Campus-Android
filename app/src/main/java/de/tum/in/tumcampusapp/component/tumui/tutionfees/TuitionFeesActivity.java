package de.tum.in.tumcampusapp.component.tumui.tutionfees;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTime;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.api.tumonline.TUMOnlineConst;
import de.tum.in.tumcampusapp.component.other.generic.activity.ActivityForAccessingTumOnline;
import de.tum.in.tumcampusapp.component.tumui.tutionfees.model.TuitionList;
import de.tum.in.tumcampusapp.utils.DateUtils;

/**
 * Activity to show the user's tuition ; based on grades.java / quick solution
 */
public class TuitionFeesActivity extends ActivityForAccessingTumOnline<TuitionList> {

    private TextView amountTextView;
    private TextView deadlineTextView;
    private TextView semesterTextView;

    public TuitionFeesActivity() {
        super(TUMOnlineConst.Companion.getTUITION_FEE_STATUS(), R.layout.activity_tuitionfees);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        amountTextView = findViewById(R.id.soll);
        deadlineTextView = findViewById(R.id.deadline);
        semesterTextView = findViewById(R.id.semester);
        ((TextView) findViewById(R.id.fees_aid)).setMovementMethod(LinkMovementMethod.getInstance());

        JodaTimeAndroid.init(this);

        requestFetch();
    }

    /**
     * Handle the response by de-serializing it into model entities.
     *
     * @param tuitionList TUMOnline response
     */
    @Override
    public void onFetch(TuitionList tuitionList) {
        String amount = tuitionList.getTuitions().get(0).getSoll();
        amountTextView.setText(String.format("%s€", amount));

        Date deadline = DateUtils.getDate(tuitionList.getTuitions()
                                                 .get(0)
                                                 .getFrist());
        deadlineTextView.setText(DateFormat.getDateInstance()
                                           .format(deadline));
        semesterTextView.setText(tuitionList.getTuitions()
                                            .get(0)
                                            .getSemesterBez()
                                            .toUpperCase(Locale.getDefault()));

        if(amount.trim().equals("0")){
            amountTextView.setTextColor(getResources().getColor(R.color.sections_green));
        } else {
            // check if the deadline is less than a week from now
            DateTime nextWeek = new DateTime().plusWeeks(1);
            if(nextWeek.isAfter(deadline.getTime())){
                amountTextView.setTextColor(getResources().getColor(R.color.error));
            } else {
                amountTextView.setTextColor(getResources().getColor(R.color.black));
            }
        }

        showLoadingEnded();
    }
}
