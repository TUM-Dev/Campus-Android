package de.tum.in.tumcampus.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ExpandableListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.generic.ActivityForDownloadingExternal;
import de.tum.in.tumcampus.adapters.MoodleExapndabaleListAdapter;
import de.tum.in.tumcampus.models.managers.MockMoodleManager;
import de.tum.in.tumcampus.models.managers.MoodleManager;
import de.tum.in.tumcampus.models.managers.RealMoodleManager;

/**
 * This class is the main activity of the moodle which shows the list of the courses
 * and their descriptions to the user. Communicates with moodle via RealMoodleManager
 */
public class MoodleMainActivity extends ActivityForDownloadingExternal implements OnItemClickListener {
    //Moodle API Manager
    protected MoodleManager moodleManager;
    protected MoodleManager realManager;
    //ProgressDialog for loading
    private ProgressDialog mDialog;

    private MoodleExapndabaleListAdapter coursesAdapter;
    List<String> courseListHeaders;
    Map<String,List<String>> courseListChilds;
    ExpandableListView expListView;

    public MoodleMainActivity() {
        super("Moodle", R.layout.activity_moodle_main);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        baseSetUp();
        //TODO need login
        mDialog.show();
        realManager.requestUserToken(this, "student", "moodle");

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
                return true;
            case R.id.events:
                Intent eventIntent = new Intent(this,MoodleEventsActivity.class);
                startActivity(eventIntent);
                return true;
        }
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }
    /**
     * This method populates the data for lists which will be shown
     * on this actiivty. For now these include: course title,course description
     * the data is retrieved from moodleManager
     *
     */
    public void refreshListView() {
        emptyListViewData();
        Map<String, String> courses = (Map<String, String>) realManager.getCoursesList();
        for (Map.Entry <String, String >item: courses.entrySet()){
            List<String> temp = new ArrayList<String>();
            temp.add(item.getValue());
            courseListChilds.put(item.getKey(), temp);
            courseListHeaders.add(item.getKey());
        }
        baseSetupForListView();
        mDialog.dismiss();
        coursesAdapter.notifyDataSetChanged();
    }

    /**
     * Base setup for the Activity. All local variables are initialized here
     */
    private void baseSetUp() {
        mDialog = new ProgressDialog(this);
        mDialog.setMessage("Loading Your Courses");
        mDialog.setCancelable(true);
        mDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        //creating a mock object of moodle manager
        moodleManager = new MockMoodleManager();
        //creating realManager
        realManager = new RealMoodleManager();

        expListView = (ExpandableListView) findViewById(R.id.coursesExp);

        emptyListViewData();


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
    }

}
