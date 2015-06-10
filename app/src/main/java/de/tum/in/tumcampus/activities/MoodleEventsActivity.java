package de.tum.in.tumcampus.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import java.util.ArrayList;
import java.util.List;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.generic.ActivityForDownloadingExternal;
import de.tum.in.tumcampus.adapters.MoodleEventAdapter;
import de.tum.in.tumcampus.models.MoodleEvent;
import de.tum.in.tumcampus.models.MoodleToken;
import de.tum.in.tumcampus.models.managers.MoodleManager;
import de.tum.in.tumcampus.models.managers.MoodleUpdateListViewDelegate;
import de.tum.in.tumcampus.models.managers.RealMoodleManager;

/**
 * Created by a2k on 6/8/2015.
 * Activity for showing events of a user in moodle
 */
public class MoodleEventsActivity extends ActivityForDownloadingExternal implements AdapterView.OnItemClickListener, MoodleUpdateListViewDelegate {

    MoodleManager realManager;
    ProgressDialog mDialog;

    Intent intent;

    private String userToken;
    private RecyclerView eventsRecyclerView;
    private RecyclerView.LayoutManager eventsLayoutManager;
    private RecyclerView.Adapter eventsAdapter;
    List<MoodleEvent> userEvents;

    public MoodleEventsActivity() {
        super("MoodleEvents", R.layout.activity_moodle_events);
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        baseSetUp();
        mDialog.show();
        realManager.requestUserEvents(this);

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

    /**
     * This method populates the data for lists which will be shown
     * on this actiivty.
     * the data is retrieved from moodleManager
     *
     */
    public void refreshListView() {

        userEvents = new ArrayList<MoodleEvent>(realManager.getUserEvents());
        eventsAdapter = new MoodleEventAdapter(userEvents);
        eventsRecyclerView.setAdapter(eventsAdapter);
        eventsAdapter.notifyDataSetChanged();
        mDialog.dismiss();

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
        intent = getIntent();
        userToken = intent.getStringExtra("user_token");
        realManager = new RealMoodleManager(this);
        realManager.setMoodleUserToken(new MoodleToken());
        realManager.getMoodleUserToken().setToken(userToken);

        eventsRecyclerView = (RecyclerView) findViewById(R.id.moodleEventList);
        eventsLayoutManager = new LinearLayoutManager(this);
        eventsRecyclerView.setLayoutManager(eventsLayoutManager);

    }


}
