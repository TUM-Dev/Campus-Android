package de.tum.in.tumcampus.activities;

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
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.managers.MoodleManager;
import de.tum.in.tumcampus.models.managers.MockMoodleManager;
import de.tum.in.tumcampus.models.managers.RealMoodleManager;

/**
 * This class is the main activity of the moodle which shows the list of the courses
 * and their descriptions to the user. Communicates with moodle via RealMoodleManager
 */
public class MoodleMainActivity extends ActivityForDownloadingExternal implements OnItemClickListener {

    protected static MoodleManager moodleManager;

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

        // used for showing courses of the users
        courseListHeaders = new ArrayList<String>();
        courseListChilds = new HashMap<String, List<String>>();
        expListView = (ExpandableListView) findViewById(R.id.coursesExp);

        // creating a mock object of moodle manager
        moodleManager = new MockMoodleManager();

        //creating realManager
        MoodleManager realManager = new RealMoodleManager();
        //TODO need login
        realManager.requestUserToken(this, "student", "moodle");

        // populate the adapter with the data returned from moodleManager
        prepareCoursesData();

        coursesAdapter = new MoodleExapndabaleListAdapter(this, courseListHeaders, courseListChilds);
        expListView.setAdapter(coursesAdapter);
    }

    private void prepareCoursesData() {
        /**
         * This method populates the data for lists which will be shown
         * on this actiivty. For now these include: course title,course description
         * the data is retrieved from moodleManager
         *
         */
        Map<String, String> courses = ( Map<String, String> )moodleManager.getCoursesList();

        for (Map.Entry <String, String >item: courses.entrySet()){
            List<String> temp = new ArrayList<String>();
            temp.add(item.getValue());
            courseListChilds.put(item.getKey(), temp);
            courseListHeaders.add(item.getKey());
        }
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
}
