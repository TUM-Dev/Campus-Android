package de.tum.in.tumcampusapp.component.ui.studyroom;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.api.app.TUMCabeClient;
import de.tum.in.tumcampusapp.api.app.exception.NoNetworkConnectionException;
import de.tum.in.tumcampusapp.component.other.generic.activity.ProgressActivity;
import de.tum.in.tumcampusapp.component.tumui.roomfinder.RoomFinderActivity;
import de.tum.in.tumcampusapp.component.ui.studyroom.model.StudyRoomGroup;
import de.tum.in.tumcampusapp.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Shows information about reservable study rooms.
 */
public class StudyRoomsActivity extends ProgressActivity
        implements AdapterView.OnItemSelectedListener {

    private List<StudyRoomGroup> mStudyRoomGroupList;
    private int mSelectedStudyRoomGroupId = -1;
    private ViewPager mViewPager;
    private StudyRoomsPagerAdapter mSectionsPagerAdapter;

    public StudyRoomsActivity() {
        super(R.layout.activity_study_rooms);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewPager = findViewById(R.id.pager);
        loadStudyRooms();
    }

    @Override
    public void onRefresh() {
        loadStudyRooms();
    }

    private void selectCurrentSpinnerItem(Spinner spinner) {
        for (int i = 0; i < mStudyRoomGroupList.size(); i++) {
            StudyRoomGroup s = mStudyRoomGroupList.get(i);
            if (mSelectedStudyRoomGroupId == -1 || mSelectedStudyRoomGroupId == s.getId()) {
                mSelectedStudyRoomGroupId = s.getId();
                spinner.setSelection(i);
                return;
            }
        }
    }

    private Spinner getStudyRoomGroupsSpinner() {
        // Adapter for drop-down navigation
        SpinnerAdapter adapterCafeterias =
                new ArrayAdapter<StudyRoomGroup>(this, R.layout.simple_spinner_item_actionbar,
                                                 android.R.id.text1, mStudyRoomGroupList) {
                    final LayoutInflater inflater = LayoutInflater.from(getContext());

                    @Override
                    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                        View v = inflater.inflate(
                                R.layout.simple_spinner_dropdown_item_actionbar,
                                                  parent, false);
                        StudyRoomGroup studyRoomGroup = getItem(position);

                        TextView nameTextView = v.findViewById(android.R.id.text1);
                        TextView detailsTextView = v.findViewById(android.R.id.text2);

                        if (studyRoomGroup != null) {
                            String name = studyRoomGroup.getName();
                            String details = studyRoomGroup.getDetails();

                            nameTextView.setText(name);
                            detailsTextView.setText(details);

                            if (details.isEmpty()) {
                                detailsTextView.setVisibility(View.GONE);
                            }
                        }

                        return v;
                    }
                };

        Spinner spinner = findViewById(R.id.spinnerToolbar);
        spinner.setAdapter(adapterCafeterias);
        spinner.setOnItemSelectedListener(this);
        return spinner;
    }

    /**
     * A new study room group has been selected -> Switch.
     *
     * @param parent the parent view
     * @param pos    index of the new selection
     * @param id     id of the selected item
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        mSelectedStudyRoomGroupId = mStudyRoomGroupList.get(pos).getId();

        if (mSectionsPagerAdapter == null) {
            setupViewPagerAdapter(mSelectedStudyRoomGroupId);
        } else {
            changeViewPagerAdapter(mSelectedStudyRoomGroupId);
        }
    }

    private void changeViewPagerAdapter(int selectedRoomGroupId) {
        mViewPager.setAdapter(null); //unset the adapter for updating
        mSectionsPagerAdapter.setStudyRoomGroupId(selectedRoomGroupId);
        mViewPager.setAdapter(mSectionsPagerAdapter);
    }

    private void setupViewPagerAdapter(int selectedRoomGroupId) {
        mSectionsPagerAdapter = new StudyRoomsPagerAdapter(getSupportFragmentManager());
        changeViewPagerAdapter(selectedRoomGroupId);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        //
    }

    public void openLink(View view) {
        String link = (String) view.getTag();
        String roomCode = link.substring(link.indexOf(' ') + 1, link.length());

        Intent findStudyRoomIntent = new Intent();
        findStudyRoomIntent.putExtra(SearchManager.QUERY, roomCode);
        findStudyRoomIntent.setClass(this, RoomFinderActivity.class);
        startActivity(findStudyRoomIntent);
    }

    private void loadStudyRooms() {
        showLoadingStart();
        TUMCabeClient
                .getInstance(this)
                .getStudyRoomGroups(new Callback<List<StudyRoomGroup>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<StudyRoomGroup>> call,
                                           @NonNull Response<List<StudyRoomGroup>> response) {
                        List<StudyRoomGroup> groups = response.body();
                        if (!response.isSuccessful() || groups == null) {
                            showErrorLayout();
                            return;
                        }

                        if (!groups.isEmpty()) {
                            onDownloadSuccessful(groups);
                        } else {
                            showError(R.string.error_no_data_to_show);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<StudyRoomGroup>> call,
                                          @NonNull Throwable t) {
                        Utils.log(t);
                        if (t instanceof NoNetworkConnectionException) {
                            showNoInternetLayout();
                        } else {
                            showErrorLayout();
                        }
                    }
                });
    }

    private void onDownloadSuccessful(@NonNull List<StudyRoomGroup> groups) {
        Handler handler = new Handler();
        handler.post(() -> {
            updateDatabase(groups);
            runOnUiThread(() -> displayStudyRooms(groups));
        });
    }

    private void updateDatabase(@NonNull List<StudyRoomGroup> groups) {
        StudyRoomGroupManager studyRoomGroupManager = new StudyRoomGroupManager(this);
        studyRoomGroupManager.updateDatabase(groups);
    }

    private void displayStudyRooms(@NonNull List<StudyRoomGroup> studyRoomGroups) {
        mStudyRoomGroupList = studyRoomGroups;

        if (mStudyRoomGroupList.isEmpty()) {
            showErrorLayout();
            return;
        }

        Spinner spinner = getStudyRoomGroupsSpinner();
        selectCurrentSpinnerItem(spinner);
        showLoadingEnded();
    }

}
