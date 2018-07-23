package de.tum.in.tumcampusapp.component.tumui.lectures.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.Collections;
import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.api.tumonline.CacheControl;
import de.tum.in.tumcampusapp.component.other.generic.activity.ActivityForSearchingTumOnline;
import de.tum.in.tumcampusapp.component.other.generic.adapter.NoResultsAdapter;
import de.tum.in.tumcampusapp.component.tumui.lectures.LectureSearchSuggestionProvider;
import de.tum.in.tumcampusapp.component.tumui.lectures.adapter.LecturesListAdapter;
import de.tum.in.tumcampusapp.component.tumui.lectures.model.Lecture;
import de.tum.in.tumcampusapp.component.tumui.lectures.model.LecturesResponse;
import retrofit2.Call;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * This activity presents the user's lectures. The results can be filtered by the semester.
 * <p>
 * This activity uses the same models as FindLectures.
 * <p>
 * HINT: a TUMOnline access token is needed
 */
public class LecturesPersonalActivity extends ActivityForSearchingTumOnline<LecturesResponse> {

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
            intent.putExtra(Lecture.STP_SP_NR, item.getStp_sp_nr());

            startActivity(intent);
        });

        onStartSearch();
    }

    @Override
    public void onRefresh() {
        loadPersonalLectures(CacheControl.BYPASS_CACHE);
    }

    @Override
    protected void onStartSearch() {
        enableRefresh();
        loadPersonalLectures(CacheControl.USE_CACHE);
    }

    @Override
    protected void onStartSearch(String query) {
        disableRefresh();
        searchLectures(query);
    }

    private void loadPersonalLectures(CacheControl cacheControl) {
        Call<LecturesResponse> apiCall = apiClient.getPersonalLectures(cacheControl);
        fetch(apiCall);
    }

    private void searchLectures(String query) {
        Call<LecturesResponse> apiCall = apiClient.searchLectures(query);
        fetch(apiCall);
    }

    @Override
    protected void onDownloadSuccessful(@NonNull LecturesResponse response) {
        if (response.getLectures().isEmpty()) {
            lvMyLecturesList.setAdapter(new NoResultsAdapter(this));
        } else {
            List<Lecture> lectures = response.getLectures();
            Collections.sort(lectures);
            lvMyLecturesList.setAdapter(new LecturesListAdapter(this, lectures));
        }
    }

}
