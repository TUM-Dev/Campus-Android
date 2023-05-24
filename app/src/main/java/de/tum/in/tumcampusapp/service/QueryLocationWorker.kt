package de.tum.`in`.tumcampusapp.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.database.sqlite.SQLiteException
import androidx.core.app.JobIntentService
import androidx.core.content.ContextCompat
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import de.tum.`in`.tumcampusapp.component.other.locations.LocationManager
import de.tum.`in`.tumcampusapp.component.tumui.calendar.CalendarController
import de.tum.`in`.tumcampusapp.component.tumui.calendar.model.CalendarItem
import de.tum.`in`.tumcampusapp.component.tumui.lectures.model.RoomLocations
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Utils
import de.tum.`in`.tumcampusapp.utils.sync.SyncManager
import org.jetbrains.anko.doAsync

class QueryLocationsService(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    private lateinit var locationManager: LocationManager

    override fun doWork(): Result {
        loadGeo()
        return Result.success()
    }

    private fun loadGeo() {
        val calendarDao = TcaDb.getInstance(applicationContext).calendarDao()
        val roomLocationsDao = TcaDb.getInstance(applicationContext).roomLocationsDao()

        calendarDao.lecturesWithoutCoordinates
                .filter { it.location.isNotEmpty() }
                .mapNotNull { createRoomLocationsOrNull(it) }
                .also { roomLocationsDao.insert(*it.toTypedArray()) }

        // Do sync of google calendar if necessary
        val shouldSyncCalendar = Utils.getSettingBool(applicationContext, Const.SYNC_CALENDAR, false) &&
                ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED
        val syncManager = SyncManager(applicationContext)
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

    private fun createRoomLocationsOrNull(item: CalendarItem): RoomLocations? {
        val geo = locationManager.roomLocationStringToGeo(item.location)
        return geo?.let {
            RoomLocations(item.location, it)
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
