package de.tum.in.tumcampusapp.activities;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.BaseActivity;
import de.tum.in.tumcampusapp.adapters.NotificationSettingsListAdapter;
import de.tum.in.tumcampusapp.auxiliary.CafeteriaNotificationSettings;

/**
 * This activity enables the user to set a preferred notification time for a day of the week.
 * The actual local storage of the preferences is done in the CafeteriaNotificationSettings class.
 */

public class CafeteriaNotificationSettingsActivity extends BaseActivity {
    private ArrayList<Pair<Integer,Integer>> dailySchedule = new ArrayList<>();
    private CafeteriaNotificationSettings cafeteriaNotificationSettings;
    public CafeteriaNotificationSettingsActivity(){
        super(R.layout.activity_cafeteria_notification_settings);
    }

    @Override
    protected void onCreate(Bundle b){
        super.onCreate(b);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        cafeteriaNotificationSettings = new CafeteriaNotificationSettings(this);
        setupList();
        ListView listView = (ListView)findViewById(R.id.activity_notification_settings_listview);
        listView.setAdapter(new NotificationSettingsListAdapter(this,dailySchedule));
        Button save = (Button) findViewById(R.id.notification_settings_save);
        save.setOnClickListener(new SaveButtonListener(this));
    }

    private class SaveButtonListener implements View.OnClickListener{
        private Context context;
        public SaveButtonListener(Context context){
            this.context = context;
        }

        @Override
        public void onClick(View view) {
            cafeteriaNotificationSettings.saveWholeSchedule(dailySchedule);
            Toast.makeText(context, context.getString(R.string.ok), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupList();
    }

    /**
     * Reloads the settings into the dailySchedule list.
     */
    public void setupList(){
        Calendar it = Calendar.getInstance();
        it.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        int i = 0;
        while (it.get(Calendar.DAY_OF_WEEK) < Calendar.SATURDAY){
            if (i < dailySchedule.size()){
                dailySchedule.set(i,cafeteriaNotificationSettings.retrieveHourMinute(it));
            }else{
                dailySchedule.add(i,cafeteriaNotificationSettings.retrieveHourMinute(it));
            }
            it.add(Calendar.DAY_OF_WEEK,1);
            i++;
        }
    }
}
