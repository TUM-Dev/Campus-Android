package de.tum.in.tumcampus.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import java.util.List;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.generic.ActivityForDownloadingExternal;
import de.tum.in.tumcampus.adapters.MoodleEventAdapter;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.MoodleEvent;
import de.tum.in.tumcampus.models.managers.MockMoodleManager;
import de.tum.in.tumcampus.models.managers.MoodleManager;

/**
 * Created by a2k on 6/8/2015.
 * Activity for showing events of a user in moodle
 */
public class MoodleEventsActivity extends ActivityForDownloadingExternal implements AdapterView.OnItemClickListener {

    MoodleManager moodleManager;
    private RecyclerView eventsRecyclerView;
    private RecyclerView.LayoutManager eventsLayoutManager;
    private RecyclerView.Adapter eventsAdapter;

    public MoodleEventsActivity() {
        super("MoodleEvents", R.layout.activity_moodle_events);
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        // creating mockMoodleManager
        moodleManager = new MockMoodleManager();

        eventsRecyclerView = (RecyclerView) findViewById(R.id.moodleEventList);
        eventsLayoutManager = new LinearLayoutManager(this);
        eventsRecyclerView.setLayoutManager(eventsLayoutManager);

        //getting events from moodle manager
        List<MoodleEvent> user_events = moodleManager.getUserEvents();
        eventsAdapter = new MoodleEventAdapter(user_events);
        eventsRecyclerView.setAdapter(eventsAdapter);

        Utils.log(user_events.toString());

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
                Intent coursesIntent = new Intent(this,MoodleMainActivity.class);
                startActivity(coursesIntent);
                return true;
            case R.id.events:
                // Do nothing
                return true;
        }
        return false;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //TODO add to calanedar of system
    }
}
