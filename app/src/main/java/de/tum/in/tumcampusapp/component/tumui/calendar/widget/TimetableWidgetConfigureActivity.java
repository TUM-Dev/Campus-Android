package de.tum.in.tumcampusapp.component.tumui.calendar.widget;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.ListView;

import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.tumui.calendar.CalendarController;
import de.tum.in.tumcampusapp.component.tumui.calendar.model.CalendarItem;
import de.tum.in.tumcampusapp.component.tumui.lectures.adapter.LectureListSelectionAdapter;

public class TimetableWidgetConfigureActivity extends AppCompatActivity {

    private int appWidgetId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timetable_widget_configure);

        // Setup toolbar and save button
        setSupportActionBar(findViewById(R.id.main_toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_check);

        // Get appWidgetId from intent
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            appWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        ListView listViewLectures = findViewById(R.id.activity_timetable_lectures);

        // Initialize stations adapter
        CalendarController calendarController = new CalendarController(this);
        List<CalendarItem> lectures = calendarController.getLecturesForWidget(this.appWidgetId);
        listViewLectures.setAdapter(new LectureListSelectionAdapter(this, lectures, this.appWidgetId));
        listViewLectures.requestFocus();
    }

    /**
     * Setup cancel and back action
     *
     * @param item the menu item which has been pressed (or activated)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            saveAndReturn();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Saves the selection to the database, triggers a widget update and closes this activity
     */
    private void saveAndReturn() {
        // update widget
        Intent reloadIntent = new Intent(this, TimetableWidget.class);
        reloadIntent.setAction(TimetableWidget.BROADCAST_UPDATE_TIMETABLE_WIDGETS);
        reloadIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        sendBroadcast(reloadIntent);

        // return to widget
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    }

}
