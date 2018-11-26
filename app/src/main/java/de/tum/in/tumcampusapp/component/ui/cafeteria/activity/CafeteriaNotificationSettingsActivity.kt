package de.tum.`in`.tumcampusapp.component.ui.cafeteria.activity

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.generic.activity.BaseActivity
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.CafeteriaNotificationSettings
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.CafeteriaNotificationSettingsAdapter
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.CafeteriaNotificationTime
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.controller.CafeteriaMenuManager
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.di.CafeteriaModule
import kotlinx.android.synthetic.main.activity_cafeteria_notification_settings.*
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants
import javax.inject.Inject

/**
 * This activity enables the user to set a preferred notification time for a day of the week.
 * The actual local storage of the preferences is done in the CafeteriaNotificationSettings class.
 */
class CafeteriaNotificationSettingsActivity : BaseActivity(R.layout.activity_cafeteria_notification_settings) {

    @Inject
    lateinit var notificationSettings: CafeteriaNotificationSettings

    @Inject
    lateinit var cafeteriaMenuManager: CafeteriaMenuManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injector.cafeteriaComponent()
                .cafeteriaModule(CafeteriaModule(this))
                .build()
                .inject(this)

        val layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        notificationSettingsRecyclerView.layoutManager = layoutManager
        notificationSettingsRecyclerView.setHasFixedSize(true)

        val dailySchedule = buildDailySchedule(notificationSettings)

        val adapter = CafeteriaNotificationSettingsAdapter(this, notificationSettings, dailySchedule)
        notificationSettingsRecyclerView.adapter = adapter

        notificationSettingsSaveButton.setOnClickListener {
            notificationSettings.ifNotificationTimesDidChange(dailySchedule) {
                cafeteriaMenuManager.scheduleNotificationAlarms()
            }
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
