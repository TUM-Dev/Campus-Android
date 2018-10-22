package de.tum.in.tumcampusapp.component.tumui.lectures.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.api.tumonline.CacheControl;
import de.tum.in.tumcampusapp.component.other.generic.activity.ActivityForAccessingTumOnline;
import de.tum.in.tumcampusapp.component.tumui.lectures.adapter.LectureAppointmentsListAdapter;
import de.tum.in.tumcampusapp.component.tumui.lectures.model.LectureAppointment;
import de.tum.in.tumcampusapp.component.tumui.lectures.model.LectureAppointmentsResponse;
import de.tum.in.tumcampusapp.utils.Const;
import retrofit2.Call;

/**
 * This activity provides the appointment dates to a given lecture using the
 * TUMOnline web service.
 * <p>
 * HINT: a valid TUM Online token is needed
 * <p>
 * NEEDS: stp_sp_nr and title set in incoming bundle (lecture id, title)
 */
public class LecturesAppointmentsActivity
        extends ActivityForAccessingTumOnline<LectureAppointmentsResponse> {

    private ListView lvTermine;

    private String lectureId;

    public LecturesAppointmentsActivity() {
        super(R.layout.activity_lecturesappointments);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        lvTermine = findViewById(R.id.lvTerminList);

        String title = getIntent().getStringExtra(Const.TITLE_EXTRA);
        TextView tvTermineLectureName = findViewById(R.id.tvTermineLectureName);
        tvTermineLectureName.setText(title);

        lectureId = getIntent().getStringExtra("stp_sp_nr");
        if (lectureId == null) {
            finish();
            return;
        }

        loadLectureAppointments(lectureId, CacheControl.USE_CACHE);
    }

    @Override
    public void onRefresh() {
        if (lectureId != null) {
            loadLectureAppointments(lectureId, CacheControl.BYPASS_CACHE);
        }
    }

    private void loadLectureAppointments(@NonNull String lectureId, CacheControl cacheControl) {
        Call<LectureAppointmentsResponse> apiCall =
                getApiClient().getLectureAppointments(lectureId, cacheControl);
        fetch(apiCall);
    }

    @Override
    public void onDownloadSuccessful(@NonNull LectureAppointmentsResponse response) {
        List<LectureAppointment> appointments = response.getLectureAppointments();
        if (appointments == null || appointments.isEmpty()) {
            showError(R.string.no_appointments); // TODO Why is this not shown?
            return;
        }

        lvTermine.setAdapter(new LectureAppointmentsListAdapter(this, appointments));
    }

}
