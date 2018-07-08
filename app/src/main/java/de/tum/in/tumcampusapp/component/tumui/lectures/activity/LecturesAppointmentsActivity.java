package de.tum.in.tumcampusapp.component.tumui.lectures.activity;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.api.tumonline.TUMOnlineClient;
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
public class LecturesAppointmentsActivity extends ActivityForAccessingTumOnline {

    private ListView lvTermine;

    public LecturesAppointmentsActivity() {
        super(R.layout.activity_lecturesappointments);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        lvTermine = findViewById(R.id.lvTerminList);

        String title = getIntent().getStringExtra(Const.TITLE_EXTRA).toUpperCase();
        TextView tvTermineLectureName = findViewById(R.id.tvTermineLectureName);
        tvTermineLectureName.setText(title);

        String lectureId = getIntent().getStringExtra("stp_sp_nr");
        if (lectureId == null) {
            finish();
            return;
        }

        loadLectureAppointments(lectureId);
    }

    private void loadLectureAppointments(String lectureId) {
        Call<LectureAppointmentsResponse> apiCall = TUMOnlineClient
                .getInstance(this)
                .getLectureAppointments(lectureId);

        fetch(apiCall, this::handleDownloadSuccess);

        /*
                .enqueue(new Callback<LectureAppointmentsResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<LectureAppointmentsResponse> call,
                                           @NonNull Response<LectureAppointmentsResponse> response) {
                        LectureAppointmentsResponse appointmentsResponse = response.body();
                        if (appointmentsResponse != null) {
                            handleDownloadSuccess(appointmentsResponse);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<LectureAppointmentsResponse> call, @NonNull Throwable t) {
                        onDownloadError(t);
                    }
                });
        */
    }

    public void handleDownloadSuccess(LectureAppointmentsResponse lecturesList) {
        List<LectureAppointment> appointments = lecturesList.getLectureAppointments();
        if (appointments == null || appointments.isEmpty()) {
            showError(R.string.no_appointments);
            return;
        }

        lvTermine.setAdapter(new LectureAppointmentsListAdapter(this, appointments));
    }
}
