package de.tum.in.tumcampus.activities;

import android.os.Bundle;
import android.view.Menu;
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

    //RealMoodleManager moodleManager;
    MockMoodleManager moodleManager;

    private MoodleExapndabaleListAdapter coursesAdapter;
    List<String> listDataHeaders;
    Map<String,List<String>> listDataChild;
    ExpandableListView expListView;

    public MoodleMainActivity() {
        super("Moodle", R.layout.activity_moodle_main);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        listDataHeaders = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();
        moodleManager = new MockMoodleManager();
        expListView = (ExpandableListView) findViewById(R.id.moodleExp);

        // populate the adapter with the data returned from moodle
        prepareCoursesData();

        coursesAdapter = new MoodleExapndabaleListAdapter(this, listDataHeaders, listDataChild);
        expListView.setAdapter(coursesAdapter);

        Utils.log("here is the list of the courses....");
        Utils.log(String.valueOf(coursesAdapter.getGroupCount()));
        Utils.log(listDataHeaders.toString());
        MoodleManager realManager = new RealMoodleManager();
        //moodleManager.requestUserToken("student","moodle",this);
        //TODO need login
        realManager.requestUserToken(this, "student", "moodle");


    }

    private void prepareCoursesData() {
        /**
         * This method populates the data for lists which will be shown
         * on this actiivty. For now these include: course title,course description
         *
         */
        Map<String, String> courses = ( Map<String, String> )moodleManager.getCoursesList();


        for (Map.Entry <String, String >item: courses.entrySet()){
            List<String> temp = new ArrayList<String>();
            temp.add(item.getValue());
            listDataChild.put(item.getKey(), temp);
            listDataHeaders.add(item.getKey());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_moodle, menu);
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }
}
