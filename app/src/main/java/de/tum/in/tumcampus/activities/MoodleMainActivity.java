package de.tum.in.tumcampus.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.generic.ActivityForDownloadingExternal;
import de.tum.in.tumcampus.models.managers.MoodleManager;

public class MoodleMainActivity extends ActivityForDownloadingExternal implements OnItemClickListener {
    MoodleManager moodleManager;



    public MoodleMainActivity() {
        super("Moodle", R.layout.activity_moodle_main);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_moodle_main);
        moodleManager = new MoodleManager();
        //moodleManager.requestUserToken("student","moodle",this);
        moodleManager.requestServiceCall(this, "username=student&password=moodle&service=moodle_mobile_app");

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
