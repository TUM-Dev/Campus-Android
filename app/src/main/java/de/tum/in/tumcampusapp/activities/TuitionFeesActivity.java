package de.tum.in.tumcampusapp.activities;

import android.os.Bundle;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.ActivityForAccessingTumOnline;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.models.tumo.TuitionList;
import de.tum.in.tumcampusapp.tumonline.TUMOnlineConst;

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

        amountTextView = (TextView) findViewById(R.id.soll);
        deadlineTextView = (TextView) findViewById(R.id.frist);
        semesterTextView = (TextView) findViewById(R.id.semester);

        requestFetch();
    }

    /**
     * Handle the response by de-serializing it into model entities.
     *
     * @param tuitionList TUMOnline response
     */
    @Override
    public void onFetch(TuitionList tuitionList) {
        amountTextView.setText(String.format("%s€", tuitionList.getTuitions().get(0).getSoll()));
        Date date = Utils.getDate(tuitionList.getTuitions().get(0).getFrist());
        deadlineTextView.setText(SimpleDateFormat.getDateInstance().format(date));
        semesterTextView.setText(tuitionList.getTuitions().get(0).getSemesterBez().toUpperCase(Locale.getDefault()));

        showLoadingEnded();
    }
}
