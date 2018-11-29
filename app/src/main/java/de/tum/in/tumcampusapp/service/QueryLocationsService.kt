package de.tum.`in`.tumcampusapp.service

import android.Manifest.permission.WRITE_CALENDAR
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.database.sqlite.SQLiteException
import androidx.core.app.JobIntentService
import androidx.core.content.ContextCompat
import de.tum.`in`.tumcampusapp.component.other.locations.LocationManager
import de.tum.`in`.tumcampusapp.component.tumui.calendar.CalendarController
import de.tum.`in`.tumcampusapp.component.tumui.lectures.model.RoomLocations
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Utils
import de.tum.`in`.tumcampusapp.utils.sync.SyncManager
import org.jetbrains.anko.doAsync

class QueryLocationsService : JobIntentService() {

    override fun onHandleWork(intent: Intent) {
        doAsync {
            loadGeo()
        }
    }

    private fun loadGeo() {
        val locationManager = LocationManager(this)
        val calendarDao = TcaDb.getInstance(this).calendarDao()
        val roomLocationsDao = TcaDb.getInstance(this).roomLocationsDao()

        val calendarItems = calendarDao.lecturesWithoutCoordinates
        for (calendarItem in calendarItems) {
            val location = calendarItem.location
            if (location.isEmpty()) {
                continue
            }

            val geo = locationManager.roomLocationStringToGeo(location)
            geo?.let {
                Utils.logv("inserted " + location + ' '.toString() + it)
                roomLocationsDao.insert(RoomLocations(location, it))
            }
        }

        // Do sync of google calendar if necessary
        val shouldSyncCalendar = Utils.getSettingBool(this, Const.SYNC_CALENDAR, false)
                && ContextCompat.checkSelfPermission(this, WRITE_CALENDAR) == PERMISSION_GRANTED
        val syncManager = SyncManager(this)
        val needsSync = syncManager.needSync(Const.SYNC_CALENDAR, TIME_TO_SYNC_CALENDAR)

        if (shouldSyncCalendar.not() || needsSync.not()) {
            return
        }

        try {
            CalendarController.syncCalendar(this)
            syncManager.replaceIntoDb(Const.SYNC_CALENDAR)
        } catch (e: SQLiteException) {
            Utils.log(e)
        }
    }

    companion object {

        private const val TIME_TO_SYNC_CALENDAR = 604800 // 1 week

        @JvmStatic fun enqueueWork(context: Context) {
            Utils.log("Query locations work enqueued")
            JobIntentService.enqueueWork(context, QueryLocationsService::class.java,
                    Const.QUERY_LOCATIONS_SERVICE_JOB_ID, Intent())
        }

    }

}
