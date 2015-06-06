package de.tum.in.tumcampus.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.models.managers.MoodleManager;

public class MoodleMainActivity extends ActionBarActivity {
    MoodleManager moodleManager;
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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_moodle_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
