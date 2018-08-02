package de.tum.`in`.tumcampusapp.component.ui.cafeteria.activity

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.generic.activity.BaseActivity
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.CafeteriaNotificationSettings
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.CafeteriaNotificationSettingsAdapter
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.CafeteriaNotificationTime
import kotlinx.android.synthetic.main.activity_cafeteria_notification_settings.*
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants

/**
 * This activity enables the user to set a preferred notification time for a day of the week.
 * The actual local storage of the preferences is done in the CafeteriaNotificationSettings class.
 */
class CafeteriaNotificationSettingsActivity : BaseActivity(R.layout.activity_cafeteria_notification_settings) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        notificationSettingsRecyclerView.layoutManager = layoutManager
        notificationSettingsRecyclerView.setHasFixedSize(true)

        val notificationSettings = CafeteriaNotificationSettings.getInstance(this)
        val dailySchedule = buildDailySchedule(notificationSettings)

        val adapter = CafeteriaNotificationSettingsAdapter(this, dailySchedule)
        notificationSettingsRecyclerView.adapter = adapter

        notificationSettingsSaveButton.setOnClickListener {
            notificationSettings.saveEntireSchedule(dailySchedule)
            finish()
        }
    }

    /**
     * Reloads the settings into the dailySchedule list.
     */
    private fun buildDailySchedule(
            settings: CafeteriaNotificationSettings): List<CafeteriaNotificationTime> {
        return (DateTimeConstants.MONDAY until DateTimeConstants.SATURDAY)
                .map {
                    val day = DateTime.now().withDayOfWeek(it)
                    val time = settings.retrieveLocalTime(day)
                    CafeteriaNotificationTime(day, time)
                }
                .toList()
    }

}
