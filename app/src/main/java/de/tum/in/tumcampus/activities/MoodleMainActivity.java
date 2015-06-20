package de.tum.in.tumcampus.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import de.tum.in.tumcampus.activities.generic.ActivityForDownloadingExternal;
import de.tum.in.tumcampus.adapters.MoodleExapndabaleListAdapter;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.managers.MockMoodleManager;
import de.tum.in.tumcampus.models.managers.MoodleManager;
import de.tum.in.tumcampus.models.managers.MoodleUpdateDelegate;
import de.tum.in.tumcampus.models.managers.RealMoodleManager;

/**
 * This class is the main activity of the moodle which shows the list of the courses
 * and their descriptions to the user. Communicates with moodle via RealMoodleManager
 */
public class MoodleMainActivity extends ActivityForDownloadingExternal implements ExpandableListView.OnChildClickListener, MoodleUpdateDelegate {

    //Moodle API Manager
    protected MoodleManager moodleManager;
    protected MoodleManager realManager;

    //ProgressDialog for loading
    private ProgressDialog mDialog;

    private MoodleExapndabaleListAdapter coursesAdapter;
    List<String> courseListHeaders;
    Map <String, Integer> coursesIds;
    Map<String,List<String>> courseListChilds;
    ExpandableListView expListView;


    private static boolean deletingSharedPref = true;

    public MoodleMainActivity() {
        super("Moodle", R.layout.activity_moodle_main);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        baseSetUp();
        mDialog.show();

        if (deletingSharedPref){
            //just for testing login
            //at the start of the activity delete the cached token
            //inorder to bring up the login activty

            SharedPreferences sharedPreferences = getSharedPreferences(getResources().getString(R.string.moodle_user_shared_prefs_key), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(getResources().getString(R.string.moodle_token_key),null);
            editor.apply();
            deletingSharedPref = false;
        }

        // check if we need to bring up the login page
        if (! checkLoginNeeded())
            realManager.requestUserData(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_moodle, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.moodle_my_courses:
                //do nothing
                Utils.showToast(this,R.string.moodle_stay_here);
                return true;
            case R.id.events:
                Intent eventIntent = new Intent(this,MoodleEventsActivity.class);
                startActivity(eventIntent);
                return true;

            case R.id.moodle_profile:
                //TODO change this part to show user profile not course with course id=62 ! Fucker!
                return true;

        }
        return false;
    }

    /**
     * This method populates the data for lists which will be shown
     * on this actiivty. For now these include: course title,course description
     * the data is retrieved from moodleManager
     *
     */
    public void refresh() {
        emptyListViewData();
        Map<String, String> courses = (Map<String, String>) realManager.getCoursesList();


        // userinfo is still null !! make the user Login again
        if (realManager.getMoodleUserInfo() == null) {
            Utils.log("UserInfo is still null dude!");
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
        }else {

            //populate the view with user's courses
            coursesIds = (Map<String, Integer>) realManager.getCoursesId();
            for (Map.Entry<String, String> item : courses.entrySet()) {
                List<String> temp = new ArrayList<String>();
                temp.add(item.getValue());
                courseListChilds.put(item.getKey(), temp);
                courseListHeaders.add(item.getKey());
            }
            baseSetupForListView();
            mDialog.dismiss();
            coursesAdapter.notifyDataSetChanged();
        }

    }

    /**
     * Base setup for the Activity. All local variables are initialized here
     */
    private void baseSetUp() {
        mDialog = new ProgressDialog(this);
        mDialog.setMessage(getResources().getString(R.string.loading));
        mDialog.setCancelable(true);
       mDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
           @Override
           public void onClick(DialogInterface dialog, int which) {
               dialog.dismiss();
           }
       });

        //creating a mock object of moodle manager
        moodleManager = new MockMoodleManager(this);
        //creating realManager
        realManager = RealMoodleManager.getInstance(this, this);
        expListView = (ExpandableListView) findViewById(R.id.coursesExp);
        expListView.setOnChildClickListener(this);
        emptyListViewData();
    }

    private boolean checkLoginNeeded(){
        // check if the token is null
        if (realManager.getMoodleUserToken() == null) {

            // initial start of the moodle program
            Utils.log("Token of moodlemanager is null");
            // check if the token in shared pref is also null
            if (realManager.loadUserToken()) {

                // got the token from shared pref
                Utils.log("Token loaded from sharedpref is not null");
                return false;
            }
            else{

                // token is empty user should log in again
                Utils.log("starting login activity");

                Intent loginIntent = new Intent(this, MoodleLoginActivity.class);
                loginIntent.putExtra("class", this.getClass());
                loginIntent.putExtra("outside_activity", false);
                startActivity(loginIntent);
                finish();
                return true;
            }
        }else
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
        courseListHeaders = new ArrayList<String>();
        courseListChilds = new HashMap<String, List<String>>();
        coursesIds = new HashMap<String, Integer>();
    }


    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        try{
			String courseName = (String)coursesAdapter.getGroup(groupPosition);
            int courseId = coursesIds.get(courseName);

            Utils.log(courseName + " id " + courseId);

            Intent courseInfoIntent = new Intent(this, MoodleCourseInfoActivity.class);
			courseInfoIntent.putExtra("course_name", courseName);
			courseInfoIntent.putExtra("course_id", courseId);
            startActivity(courseInfoIntent);
            return true;
        }catch (Exception e){
            Utils.showToast(this, getString(R.string.moodle_courses_error));
            Utils.log(e);
            return false;
        }
    }
}
