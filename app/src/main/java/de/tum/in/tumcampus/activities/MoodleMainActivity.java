package de.tum.in.tumcampus.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.generic.ProgressActivity;
import de.tum.in.tumcampus.adapters.MoodleExapndabaleListAdapter;
import de.tum.in.tumcampus.auxiliary.NetUtils;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.managers.MoodleManager;
import de.tum.in.tumcampus.models.managers.MoodleUpdateDelegate;
import de.tum.in.tumcampus.models.managers.RealMoodleManager;

/**
 * This class is the main activity of the moodle which shows the list of the courses
 * and their descriptions to the user. Communicates with moodle via RealMoodleManager
 */
public class MoodleMainActivity extends ProgressActivity implements ExpandableListView.OnChildClickListener, MoodleUpdateDelegate {

    //Moodle API Manager
    protected MoodleManager realManager;

    private MoodleExapndabaleListAdapter coursesAdapter;
    List<String> courseListHeaders;
    Map<String, Integer> coursesIds;
    Map<String, List<String>> courseListChilds;
    ExpandableListView expListView;

    public MoodleMainActivity() {
        super(R.layout.activity_moodle_main);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        baseSetUp();
        showLoadingStart();

        // check if we need to bring up the login page
        if (!checkLoginNeeded())
            realManager.requestUserData(this);
    }

    @Override
    public void onRestart() {
        super.onRestart();
        refresh();
    }

    /**
     * this method is called
     * when internet connection is back
     */
    @Override
    public void onRefresh() {
        realManager.requestUserData(this);
        showLoadingStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_moodle, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.moodle_my_courses:
                //do nothing
                Utils.showToast(this, R.string.moodle_stay_here);
                return true;
            case R.id.events:
                Intent eventIntent = new Intent(this, MoodleEventsActivity.class);
                startActivity(eventIntent);
                finish();
                return true;
        }
        return false;
    }

    /**
     * This method populates the data for lists which will be shown
     * on this activity. It is called by Moodlemanager when the requested data
     * is ready. In this case after requesting requestUserData() after the first login
     * or requestUserCourseList()
     */
    public void refresh() {
        try {
            if (!NetUtils.isConnected(this)) {
                Utils.showToast(this, R.string.no_internet_connection);
                showNoInternetLayout();
                return;
            }

            emptyListViewData();
            Map<String, String> courses = realManager.getCoursesList();

            /* userinfo is still null ! either the token retrieved from
              shared pref is not valid or token has been expired
              make the user Login again
            */
            if (realManager.getMoodleUserInfo() == null) {
                Utils.log("starting Login Intent and finishing main activity");

                Intent intent = new Intent(this, MoodleLoginActivity.class);
                intent.putExtra("class", this.getClass());
                intent.putExtra("outside_activity", false);
                startActivity(intent);
                finish();
                return;
            }

            if (courses == null) {
                realManager.requestUserCourseList(this);
                return;
            }

            //populate the view with user's courses
            coursesIds = realManager.getCoursesId();
            for (Map.Entry<String, String> item : courses.entrySet()) {
                List<String> temp = new ArrayList<>();
                temp.add(item.getValue());
                courseListChilds.put(item.getKey(), temp);
                courseListHeaders.add(item.getKey());
            }
            baseSetupForListView();
            showLoadingEnded();
            coursesAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            Utils.log(e);
            showLoadingEnded();
            Utils.showToast(this, R.string.error_something_wrong);
        }

    }

    /**
     * Base setup for the Activity. All local variables are initialized here
     */
    private void baseSetUp() {

        //creating realManager
        realManager = RealMoodleManager.getInstance(this, this);
        expListView = (ExpandableListView) findViewById(R.id.coursesExp);
        expListView.setOnChildClickListener(this);
        emptyListViewData();
    }

    /**
     * check to see if the token has expired or not. In case of expired token
     * launch the Login Activity
     *
     * @return if a new login is needed
     */
    private boolean checkLoginNeeded() {
        // check if the token is null
        if (realManager.getMoodleUserToken() == null) {

            // initial start of the moodle program
            Utils.log("Token of moodlemanager is null");
            // check if the token in shared pref is also null
            if (realManager.loadUserToken()) {

                // got the token from shared pref
                Utils.log("Token loaded from sharedpref is not null");
                return false;
            } else {
                // token is empty user should log in again
                Utils.log("starting login activity");
                Intent loginIntent = new Intent(this, MoodleLoginActivity.class);
                loginIntent.putExtra("outside_activity", false);
                startActivity(loginIntent);
                finish();
                return true;
            }
        } else
            // activity started from other activities in moodle
            return false;
    }

    /**
     * Base setup for the listView. Assigns the List Adapter to the ListView
     */
    private void baseSetupForListView() {
        coursesAdapter = new MoodleExapndabaleListAdapter(this, courseListHeaders, courseListChilds);
        expListView.setAdapter(coursesAdapter);
    }

    /**
     * Empties the ListView preparing it for fresh data
     */
    private void emptyListViewData() {
        courseListHeaders = new ArrayList<>();
        courseListChilds = new HashMap<>();
        coursesIds = new HashMap<>();
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        try {
            String courseName = (String) coursesAdapter.getGroup(groupPosition);
            int courseId = coursesIds.get(courseName);
            Intent courseInfoIntent = new Intent(this, MoodleCourseInfoActivity.class);
            courseInfoIntent.putExtra("course_name", courseName);
            courseInfoIntent.putExtra("course_id", courseId);
            showLoadingStart();
            startActivity(courseInfoIntent);
            return true;
        } catch (Exception e) {
            Utils.showToast(this, getString(R.string.moodle_courses_error));
            Utils.log(e);
            return false;
        }
    }
}
