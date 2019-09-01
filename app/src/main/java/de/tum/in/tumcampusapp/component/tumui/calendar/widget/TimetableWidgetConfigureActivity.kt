package de.tum.`in`.tumcampusapp.component.tumui.calendar.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.tumui.calendar.CalendarController
import de.tum.`in`.tumcampusapp.component.tumui.lectures.adapter.LectureListSelectionAdapter

class TimetableWidgetConfigureActivity : AppCompatActivity() {

    private var appWidgetId: Int = 0

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timetable_widget_configure)

        // Setup toolbar and save button
        setSupportActionBar(findViewById(R.id.toolbar))

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val closeIcon = ContextCompat.getDrawable(this, R.drawable.ic_check)
        if (closeIcon != null) {
            val color = ContextCompat.getColor(this, R.color.color_primary)
            closeIcon.setTint(color)
        }
        supportActionBar?.setHomeAsUpIndicator(closeIcon)

        // Get appWidgetId from intent
        appWidgetId = intent.extras?.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID) ?: 0

        val listViewLectures = findViewById<ListView>(R.id.activity_timetable_lectures)

        // Initialize stations adapter
        val calendarController = CalendarController(this)
        val lectures = calendarController.getLecturesForWidget(this.appWidgetId)
        listViewLectures.adapter = LectureListSelectionAdapter(this, lectures, this.appWidgetId)
        listViewLectures.requestFocus()
    }

    /**
     * Setup cancel and back action
     *
     * @param item the menu item which has been pressed (or activated)
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            saveAndReturn()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Saves the selection to the database, triggers a widget update and closes this activity
     */
    private fun saveAndReturn() {
        // update widget
        val reloadIntent = Intent(this, TimetableWidget::class.java).apply {
            action = TimetableWidget.BROADCAST_UPDATE_TIMETABLE_WIDGETS
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        sendBroadcast(reloadIntent)

        // return to widget
        val resultValue = Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        setResult(Activity.RESULT_OK, resultValue)
        finish()
    }
}
