package de.tum.in.tumcampusapp.component.tumui.lectures.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import java.util.Locale;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.api.tumonline.TUMOnlineClient;
import de.tum.in.tumcampusapp.component.other.generic.activity.ActivityForAccessingTumOnline;
import de.tum.in.tumcampusapp.component.tumui.lectures.model.LectureDetails;
import de.tum.in.tumcampusapp.component.tumui.lectures.model.LectureDetailsResponse;
import de.tum.in.tumcampusapp.utils.Const;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * This Activity will show all details found on the TUMOnline web service
 * identified by its lecture id (which has to be posted to this activity by
 * bundle).
 * <p/>
 * There is also the opportunity to get all appointments which are related to
 * this lecture by clicking the button on top of the view.
 * <p/>
 * HINT: a valid TUM Online token is needed
 * <p/>
 * NEEDS: stp_sp_nr set in incoming bundle (lecture id)
 */
public class LecturesDetailsActivity extends ActivityForAccessingTumOnline implements OnClickListener {

    /**
     * UI elements
     */
    private Button btnLDetailsTermine;

    /**
     * the current processing Lecture item (model: LectureDetailsRow)
     */
    private LectureDetails currentItem;
    private TextView tvLDetailsDozent;
    private TextView tvLDetailsInhalt;
    private TextView tvLDetailsLiteratur;
    private TextView tvLDetailsMethode;
    private TextView tvLDetailsName;
    private TextView tvLDetailsOrg;
    private TextView tvLDetailsSemester;
    private TextView tvLDetailsSWS;
    private TextView tvLDetailsTermin;
    private TextView tvLDetailsZiele;

    public LecturesDetailsActivity() {
        super(R.layout.activity_lecturedetails);
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        if (view.getId() == btnLDetailsTermine.getId()) {
            // start LectureAppointments
            Bundle bundle = new Bundle();
            // LectureAppointments need the name and id of the facing lecture
            bundle.putString("stp_sp_nr", currentItem.getStp_sp_nr());
            bundle.putString(Const.TITLE_EXTRA, currentItem.getTitle());

            Intent i = new Intent(this, LecturesAppointmentsActivity.class);
            i.putExtras(bundle);
            startActivity(i);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tvLDetailsName = findViewById(R.id.tvLDetailsName);
        tvLDetailsSWS = findViewById(R.id.tvLDetailsSWS);
        tvLDetailsSemester = findViewById(R.id.tvLDetailsSemester);
        tvLDetailsDozent = findViewById(R.id.tvLDetailsDozent);
        tvLDetailsOrg = findViewById(R.id.tvLDetailsOrg);
        tvLDetailsInhalt = findViewById(R.id.tvLDetailInhalt);
        tvLDetailsMethode = findViewById(R.id.tvLDetailsMethode);
        tvLDetailsZiele = findViewById(R.id.tvLDetailsZiele);
        tvLDetailsTermin = findViewById(R.id.tvLDetailsTermin);
        tvLDetailsLiteratur = findViewById(R.id.tvLDetailsLiteratur);
        btnLDetailsTermine = findViewById(R.id.btnLDetailsTermine);
        btnLDetailsTermine.setOnClickListener(this);

        Bundle bundle = this.getIntent()
                            .getExtras();

        String lectureId = getIntent().getStringExtra("stp_sp_nr");
        if (lectureId == null) {
            finish();
            return;
        }

        loadLectureDetails(lectureId);
    }

    // TODO: Pull-to-refresh

    private void loadLectureDetails(@NonNull String lectureId) {
        showLoadingStart();
        TUMOnlineClient
                .getInstance(this)
                .getLectureDetails(lectureId)
                .enqueue(new Callback<LectureDetailsResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<LectureDetailsResponse> call,
                                           @NonNull Response<LectureDetailsResponse> response) {

                    }

                    @Override
                    public void onFailure(@NonNull Call<LectureDetailsResponse> call, @NonNull Throwable t) {
                        handleDownloadError(t);
                    }
                });
    }

    public void handleDownloadSuccess(LectureDetailsResponse lectureDetailsResponse) {
        showLoadingEnded();

        // we got exactly one row, that's fine
        currentItem = lectureDetailsResponse.getLectureDetails().get(0);
        tvLDetailsName.setText(currentItem.getTitle().toUpperCase(Locale.getDefault()));

        StringBuilder strLectureLanguage = new StringBuilder(currentItem.getSemesterName());
        if (currentItem.getMainLanguage() != null) {
            strLectureLanguage.append(" - ")
                              .append(currentItem.getMainLanguage());
        }
        tvLDetailsSemester.setText(strLectureLanguage);
        tvLDetailsSWS.setText(String.format("%s - %s SWS", currentItem.getLectureType(), currentItem.getDuration()));
        tvLDetailsDozent.setText(currentItem.getLecturers());
        tvLDetailsOrg.setText(currentItem.getChairName());
        tvLDetailsInhalt.setText(currentItem.getLectureContent());
        tvLDetailsMethode.setText(currentItem.getTeachingMethod());
        tvLDetailsZiele.setText(currentItem.getTeachingTargets());
        tvLDetailsLiteratur.setText(currentItem.getExaminationAids());
        tvLDetailsTermin.setText(currentItem.getFirstAppointment());

        showLoadingEnded();
    }

}
