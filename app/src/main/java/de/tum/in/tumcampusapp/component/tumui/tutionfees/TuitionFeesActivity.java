package de.tum.in.tumcampusapp.component.tumui.tutionfees;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTime;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.api.tumonline.TUMOnlineClient;
import de.tum.in.tumcampusapp.component.other.generic.activity.ProgressActivity;
import de.tum.in.tumcampusapp.component.tumui.tutionfees.model.Tuition;
import de.tum.in.tumcampusapp.component.tumui.tutionfees.model.TuitionList;
import de.tum.in.tumcampusapp.utils.DateUtils;
import de.tum.in.tumcampusapp.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity to show the user's tuition ; based on grades.java / quick solution
 */
public class TuitionFeesActivity extends ProgressActivity {

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

        JodaTimeAndroid.init(this);
        refreshData();
    }

    @Override
    public void onRefresh() {
        refreshData();
    }

    private void refreshData() {
        TUMOnlineClient
                .getInstance(this)
                .getTuitionFeesStatus()
                .enqueue(new Callback<TuitionList>() {
                    @Override
                    public void onResponse(@NonNull Call<TuitionList> call,
                                           @NonNull Response<TuitionList> response) {
                        TuitionList tuitionList = response.body();
                        if (tuitionList != null) {
                            displayTuition(tuitionList);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<TuitionList> call, @NonNull Throwable t) {
                        Utils.log(t);
                        handleDownloadError();
                    }
                });
    }

    private void displayTuition(@NonNull TuitionList tuitionList) {
        Tuition tuition = tuitionList.getTuitions().get(0);

        String amountText = tuition.getOutstandingBalanceText();
        amountTextView.setText(amountText);

        Date deadline = DateUtils.getDate(tuitionList.getTuitions()
                .get(0)
                .getFrist());
        deadlineTextView.setText(DateFormat.getDateInstance()
                .format(deadline));
        semesterTextView.setText(tuitionList.getTuitions()
                .get(0)
                .getSemesterBez()
                .toUpperCase(Locale.getDefault()));

        if (tuition.getOutstandingBalance() == 0) {
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

    private void handleDownloadError() {
        finish();
    }

}
