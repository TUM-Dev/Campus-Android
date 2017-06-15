package de.tum.in.tumcampusapp.activities;
import android.os.Bundle;
import android.support.v4.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Calendar;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.BaseActivity;
import de.tum.in.tumcampusapp.adapters.NotificationSettingsListAdapter;
import de.tum.in.tumcampusapp.auxiliary.CafeteriaNotificationSettings;
import de.tum.in.tumcampusapp.auxiliary.Utils;


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
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Pair<Integer,Integer> hourMinute = dailySchedule.get(position);
                Utils.log("3993: "+hourMinute.first+ " "+ hourMinute.second);
            }
        });
        Button save = (Button) findViewById(R.id.notification_settings_save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cafeteriaNotificationSettings.saveWholeSchedule(dailySchedule);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupList();
    }

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
