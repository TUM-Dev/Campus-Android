package de.tum.in.tumcampusapp.component.tumui.lectures.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.button.MaterialButton;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.api.tumonline.CacheControl;
import de.tum.in.tumcampusapp.component.other.generic.activity.ActivityForAccessingTumOnline;
import de.tum.in.tumcampusapp.component.tumui.lectures.model.LectureDetails;
import de.tum.in.tumcampusapp.component.tumui.lectures.model.LectureDetailsResponse;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.Utils;
import retrofit2.Call;

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
public class LecturesDetailsActivity extends ActivityForAccessingTumOnline<LectureDetailsResponse> {

    private TextView lectureNameTextView;
    private TextView swsTextView;
    private TextView semesterTextView;
    private TextView professorTextView;
    private TextView orgTextView;
    private TextView contentTextView;
    private TextView methodTextView;
    private TextView examinationAidsTextView;
    private TextView dateTextView;
    private TextView targetsTextView;

    private LectureDetails currentItem;
    private String mLectureId;

    public LecturesDetailsActivity() {
        super(R.layout.activity_lecturedetails);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        lectureNameTextView = findViewById(R.id.lectureNameTextView);
        swsTextView = findViewById(R.id.swsTextView);
        semesterTextView = findViewById(R.id.semesterTextView);
        professorTextView = findViewById(R.id.professorTextView);
        orgTextView = findViewById(R.id.orgTextView);
        contentTextView = findViewById(R.id.contentTextView);
        methodTextView = findViewById(R.id.methodTextView);
        targetsTextView = findViewById(R.id.targetsTextView);
        dateTextView = findViewById(R.id.dateTextView);
        examinationAidsTextView = findViewById(R.id.examinationAidsTextView);

        MaterialButton appointmentsButton = findViewById(R.id.appointmentsButton);
        appointmentsButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();

            // LectureAppointments need the name and id of the facing lecture
            bundle.putString("stp_sp_nr", currentItem.getStp_sp_nr());
            bundle.putString(Const.TITLE_EXTRA, currentItem.getTitle());

            Intent i = new Intent(this, LecturesAppointmentsActivity.class);
            i.putExtras(bundle);
            startActivity(i);
        });

        mLectureId = getIntent().getStringExtra("stp_sp_nr");
        if (mLectureId == null) {
            finish();
            return;
        }

        loadLectureDetails(mLectureId, CacheControl.USE_CACHE);
    }

    @Override
    public void onRefresh() {
        loadLectureDetails(mLectureId, CacheControl.BYPASS_CACHE);
    }

    private void loadLectureDetails(@NonNull String lectureId, CacheControl cacheControl) {
        Call<LectureDetailsResponse> apiCall = getApiClient().getLectureDetails(lectureId, cacheControl);
        fetch(apiCall);
    }

    @Override
    public void onDownloadSuccessful(@NonNull LectureDetailsResponse response) {
        List<LectureDetails> lectureDetails = response.getLectureDetails();
        if (lectureDetails.isEmpty()) {
            Utils.showToast(this, R.string.error_no_data_to_show);
            finish();
            return;
        }

        currentItem = lectureDetails.get(0);
        lectureNameTextView.setText(currentItem.getTitle());

        StringBuilder strLectureLanguage = new StringBuilder(currentItem.getSemesterName());
        if (currentItem.getMainLanguage() != null) {
            strLectureLanguage.append(" - ")
                    .append(currentItem.getMainLanguage());
        }
        semesterTextView.setText(strLectureLanguage);

        swsTextView.setText(getString(R.string.lecture_details_format_string,
                currentItem.getLectureType(), currentItem.getDuration()));
        professorTextView.setText(currentItem.getLecturers());
        orgTextView.setText(currentItem.getChairName());
        contentTextView.setText(currentItem.getLectureContent());
        dateTextView.setText(currentItem.getFirstAppointment());

        String teachingMethod = currentItem.getTeachingMethod();
        if (teachingMethod == null || teachingMethod.isEmpty()) {
            findViewById(R.id.methodHeaderTextView).setVisibility(View.GONE);
            methodTextView.setVisibility(View.GONE);
        } else {
            methodTextView.setText(teachingMethod);
        }

        String targets = currentItem.getTeachingTargets();
        if (targets == null || targets.isEmpty()) {
            findViewById(R.id.targetsHeaderTextView).setVisibility(View.GONE);
            targetsTextView.setVisibility(View.GONE);
        } else {
            targetsTextView.setText(targets);
        }

        String aids = currentItem.getExaminationAids();
        if (aids == null || aids.isEmpty()) {
            findViewById(R.id.examinationAidsHeaderTextView).setVisibility(View.GONE);
            examinationAidsTextView.setVisibility(View.GONE);
        } else {
            examinationAidsTextView.setText(aids);
        }
    }

}
