package de.tum.in.tumcampus.activities;

import android.os.Bundle;
import android.view.Menu;
import android.widget.AdapterView;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;

import de.tum.in.tumcampus.R;
<<<<<<< HEAD
import de.tum.in.tumcampus.activities.generic.ActivityForDownloadingExternal;
import de.tum.in.tumcampus.auxiliary.Utils;
=======
>>>>>>> 8a6fc1c6451eae0b2ded1f894da197d9d67fd047
import de.tum.in.tumcampus.models.managers.MoodleManager;

public class MoodleMainActivity extends ActivityForDownloadingExternal implements OnItemClickListener {
    MoodleManager moodleManager;



    public MoodleMainActivity() {
        super("Moodle", R.layout.activity_moodle_main);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
<<<<<<< HEAD
   /* maybe used later. copied from news activity */
            //requestDownload(false);

            Utils.log("On Create is called!");
            moodleManager = new MoodleManager();
            moodleManager.requestUserToken("student", "moodle", this);
            Utils.log(moodleManager.getMoodleCourses(this));
=======
        setContentView(R.layout.activity_moodle_main);
        moodleManager = new MoodleManager();
        //moodleManager.requestUserToken("student","moodle",this);
        moodleManager.requestServiceCall(this, "username=student&password=moodle&service=moodle_mobile_app");
>>>>>>> 8a6fc1c6451eae0b2ded1f894da197d9d67fd047
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
