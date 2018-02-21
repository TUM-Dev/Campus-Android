package de.tum.in.tumcampusapp.component.tumui.lectures.activity;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Locale;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.api.tumonline.TUMOnlineConst;
import de.tum.in.tumcampusapp.component.other.generic.activity.ActivityForAccessingTumOnline;
import de.tum.in.tumcampusapp.component.tumui.lectures.adapter.LectureAppointmentsListAdapter;
import de.tum.in.tumcampusapp.component.tumui.lectures.model.LectureAppointmentsRowSet;
import de.tum.in.tumcampusapp.utils.Const;

/**
 * This activity provides the appointment dates to a given lecture using the
 * TUMOnline web service.
 * <p>
 * HINT: a valid TUM Online token is needed
 * <p>
 * NEEDS: stp_sp_nr and title set in incoming bundle (lecture id, title)
 */
public class LecturesAppointmentsActivity extends ActivityForAccessingTumOnline<LectureAppointmentsRowSet> {

    /**
     * UI elements
     */
    private ListView lvTermine;

    public LecturesAppointmentsActivity() {
        super(TUMOnlineConst.Companion.getLECTURES_APPOINTMENTS(), R.layout.activity_lecturesappointments);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set UI Elements
        lvTermine = findViewById(R.id.lvTerminList);
        TextView tvTermineLectureName = findViewById(R.id.tvTermineLectureName);

        Bundle bundle = this.getIntent()
                            .getExtras();
        // set Lecture Name (depends on bundle data)
        tvTermineLectureName.setText(bundle.getString(Const.TITLE_EXTRA)
                                           .toUpperCase(Locale.getDefault()));
        requestHandler.setParameter("pLVNr", bundle.getString("stp_sp_nr"));

        super.requestFetch();

    }

    /**
     * process data got from TUMOnline request and show the list view
     */
    @Override
    public void onFetch(LectureAppointmentsRowSet lecturesList) {
        // may happen if there are no appointments for the lecture
        if (lecturesList.getLehrveranstaltungenTermine() == null) {
            showError(R.string.no_appointments);
            return;
        }

        // set data to the ListView object nothing to click (yet)
        lvTermine.setAdapter(new LectureAppointmentsListAdapter(this, lecturesList.getLehrveranstaltungenTermine()));
        showLoadingEnded();
    }
}
