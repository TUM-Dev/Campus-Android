package de.tum.in.tumcampusapp.component.tumui.lectures.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.Collections;
import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.api.tumonline.TUMOnlineClient;
import de.tum.in.tumcampusapp.component.other.generic.activity.ActivityForSearchingTumOnline;
import de.tum.in.tumcampusapp.component.other.generic.adapter.NoResultsAdapter;
import de.tum.in.tumcampusapp.component.tumui.lectures.LectureSearchSuggestionProvider;
import de.tum.in.tumcampusapp.component.tumui.lectures.adapter.LecturesListAdapter;
import de.tum.in.tumcampusapp.component.tumui.lectures.model.Lecture;
import de.tum.in.tumcampusapp.component.tumui.lectures.model.LecturesResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * This activity presents the users' lectures using the TUMOnline web service
 * the results can be filtered by the semester or all shown.
 * <p>
 * This activity uses the same models as FindLectures.
 * <p>
 * HINT: a TUMOnline access token is needed
 */
public class LecturesPersonalActivity extends ActivityForSearchingTumOnline {

    /**
     * UI elements
     */
    private StickyListHeadersListView lvMyLecturesList;

    public LecturesPersonalActivity() {
        super(R.layout.activity_lectures, LectureSearchSuggestionProvider.AUTHORITY, 4);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        lvMyLecturesList = findViewById(R.id.lvMyLecturesList);

        lvMyLecturesList.setOnItemClickListener((a, v, position, id) -> {
            Object o = lvMyLecturesList.getItemAtPosition(position);
            Lecture item = (Lecture) o;

            Intent intent = new Intent(this, LecturesDetailsActivity.class);
            intent.putExtra(Lecture.Companion.getSTP_SP_NR(), item.getStp_sp_nr());

            startActivity(intent);
        });

        onStartSearch();
    }

    @Override
    public void onRefresh() {
        loadPersonalLectures();
    }

    @Override
    protected void onStartSearch() {
        enableRefresh();
        loadPersonalLectures();
    }

    @Override
    protected void onStartSearch(String query) {
        disableRefresh();
        searchLecture(query);
    }

    private void loadPersonalLectures() {
        showLoadingStart();
        TUMOnlineClient
                .getInstance(this)
                .getPersonalLectures()
                .enqueue(new Callback<LecturesResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<LecturesResponse> call,
                                           @NonNull Response<LecturesResponse> response) {
                        LecturesResponse lecturesResponse = response.body();
                        if (lecturesResponse != null) {
                            handleDownloadSuccess(lecturesResponse);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<LecturesResponse> call, @NonNull Throwable t) {
                        handleDownloadError(t);
                    }
                });
    }

    private void searchLecture(String query) {
        showLoadingStart();
        TUMOnlineClient
                .getInstance(this)
                .searchLectures(query)
                .enqueue(new Callback<LecturesResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<LecturesResponse> call,
                                           @NonNull Response<LecturesResponse> response) {
                        LecturesResponse lecturesResponse = response.body();
                        if (lecturesResponse != null) {
                            handleDownloadSuccess(lecturesResponse);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<LecturesResponse> call, @NonNull Throwable t) {
                        handleDownloadError(t);
                    }
                });
    }

    public void handleDownloadSuccess(@NonNull LecturesResponse response) {
        showLoadingEnded();
        if (response.getLectures().isEmpty()) {
            lvMyLecturesList.setAdapter(new NoResultsAdapter(this));
        } else {
            List<Lecture> lectures = response.getLectures();
            Collections.sort(lectures);
            lvMyLecturesList.setAdapter(new LecturesListAdapter(this, lectures));
        }
    }

}
