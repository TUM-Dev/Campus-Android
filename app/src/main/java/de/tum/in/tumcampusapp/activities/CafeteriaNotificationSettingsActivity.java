package de.tum.in.tumcampusapp.activities;

import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v4.util.Pair;
import android.widget.Button;
import android.widget.ListView;

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
    private final ArrayList<Pair<Integer, Integer>> dailySchedule = new ArrayList<>();
    private CafeteriaNotificationSettings cafeteriaNotificationSettings;
    private Button save;

    public CafeteriaNotificationSettingsActivity() {
        super(R.layout.activity_cafeteria_notification_settings);
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        cafeteriaNotificationSettings = new CafeteriaNotificationSettings(this);
        setupList();
        ListView listView = findViewById(R.id.activity_notification_settings_listview);
        listView.setAdapter(new NotificationSettingsListAdapter(this, dailySchedule));
        listView.getAdapter().registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                save.setBackgroundColor(getResources().getColor(R.color.tum_300));
                save.setText(R.string.save);
            }
        });
        save = findViewById(R.id.notification_settings_save);
        save.setOnClickListener(view -> {
            save.setBackgroundColor(getResources().getColor(R.color.sections_green));
            save.setText("Saved");
            cafeteriaNotificationSettings.saveWholeSchedule(dailySchedule);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupList();
    }

    /**
     * Reloads the settings into the dailySchedule list.
     */
    public void setupList() {
        Calendar it = Calendar.getInstance();
        it.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        for (int i = 0; it.get(Calendar.DAY_OF_WEEK) < Calendar.SATURDAY; i++, it.add(Calendar.DAY_OF_WEEK, 1)) {
            if (i < dailySchedule.size()) {
                dailySchedule.set(i, cafeteriaNotificationSettings.retrieveHourMinute(it));
            } else {
                dailySchedule.add(i, cafeteriaNotificationSettings.retrieveHourMinute(it));
            }
        }
    }
}
