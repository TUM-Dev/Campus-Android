package de.tum.in.tumcampus.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONObject;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.auxiliary.NetUtils;
import de.tum.in.tumcampus.auxiliary.Utils;

public class MoodleMainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.log("On Create is called!");
        setContentView(R.layout.activity_moodle_main);
        getJSON("username=student&password=moodle&service=moodle_mobile_app");
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

    /**
     * This class will handle all action needed to communicate with Moodle API.
     * All communications is based on Moodle's API system
     *
     * NOTA BENE: since the APIs aren't activated on the TUM's Moodle platform
     *            for this moment the calls will be made on the example application
     *            available in Moodle's website.
     *
     *            Orange school DEMO
     *            link:"http://school.demo.moodle.net/"
     *
     *            private static final String SERVICE_BASE_URL = "http://school.demo.moodle.net//login/token.php?";
     */
     public String getJSON(final String serviceAddress){
     //Test for moodleAPIs
     final Context currentContext = this;
     final String SERVICE_BASE_URL = "http://school.demo.moodle.net//login/token.php?";

     Thread backgroundThread = new Thread(new Runnable() {
        @Override
        public void run() {
            Utils.log(" Hello Thread!");
            try {
                JSONObject testMoodleJson = NetUtils.downloadJson(currentContext, SERVICE_BASE_URL + serviceAddress);

                Utils.log("JSON Object Moodle is: " + testMoodleJson.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    });
     backgroundThread.start();
     return "";
     }

}
