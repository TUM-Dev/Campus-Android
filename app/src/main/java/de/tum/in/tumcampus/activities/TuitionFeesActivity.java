package de.tum.in.tumcampus.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.text.SimpleDateFormat;
import java.util.Date;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.generic.ActivityForAccessingTumOnline;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.models.TuitionList;
import de.tum.in.tumcampus.tumonline.TUMOnlineRequestFetchListener;

/**
 * Activity to show the user's tuition ; based on grades.java / quick solution
 *
 * @author NTK
 */
public class TuitionFeesActivity extends ActivityForAccessingTumOnline
        implements TUMOnlineRequestFetchListener {

    private TextView amountTextView;
    private TextView deadlineTextView;
    private TextView semesterTextView;

    public TuitionFeesActivity() {
        super(Const.STUDIENBEITRAGSTATUS, R.layout.activity_tuitionfees);
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
     * Handle the response by deserializing it into model entities.
     *
     * @param rawResp TUMOnline response
     */
    @Override
    public void onFetch(String rawResp) {

        Serializer serializer = new Persister();

        try {
            TuitionList tuitionList = serializer.read(TuitionList.class, rawResp);

            amountTextView.setText(tuitionList.getTuitions().get(0).getSoll() + "€");
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            Date date = format.parse(tuitionList.getTuitions().get(0).getFrist());
            deadlineTextView.setText(SimpleDateFormat.getDateInstance().format(date));
            semesterTextView.setText(tuitionList.getTuitions().get(0).getSemesterBez().toUpperCase());

        } catch (Exception e) {
            Log.d("SIMPLEXML", "wont work: " + e.getMessage());
            errorLayout.setVisibility(View.VISIBLE);
            progressLayout.setVisibility(View.GONE);
            e.printStackTrace();
        }
        progressLayout.setVisibility(View.GONE);
    }
}
