package de.tum.`in`.tumcampusapp.service


import android.Manifest.permission.WRITE_CALENDAR
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.database.sqlite.SQLiteException
import androidx.core.app.JobIntentService
import androidx.core.content.ContextCompat
import de.tum.`in`.tumcampusapp.component.other.locations.TumLocationManager
import de.tum.`in`.tumcampusapp.component.tumui.calendar.CalendarController
import de.tum.`in`.tumcampusapp.component.tumui.calendar.model.CalendarItem
import de.tum.`in`.tumcampusapp.component.tumui.lectures.model.RoomLocation
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Utils
import de.tum.`in`.tumcampusapp.utils.injector
import de.tum.`in`.tumcampusapp.utils.sync.SyncManager
import org.jetbrains.anko.doAsync
import javax.inject.Inject

class QueryLocationsService : JobIntentService() {

    @Inject
    lateinit var tumLocationManager: TumLocationManager

    @Inject
    lateinit var database: TcaDb

    @Inject
    lateinit var syncManager: SyncManager

    @Inject
    lateinit var calendarController: CalendarController

    override fun onCreate() {
        super.onCreate()
        injector.inject(this)
    }

    override fun onHandleWork(intent: Intent) {
        doAsync {
            loadGeo()
        }
    }

    private fun loadGeo() {
        val calendarDao = TcaDb.getInstance(this).calendarDao()
        val roomLocationsDao = TcaDb.getInstance(this).roomLocationsDao()

        calendarDao.lecturesWithoutCoordinates
                .filter { it.location.isNotEmpty() }
                .mapNotNull { createRoomLocationsOrNull(it) }
                .also { roomLocationsDao.insert(*it.toTypedArray()) }

        // Do sync of google calendar if necessary
        val shouldSyncCalendar = Utils.getSettingBool(this, Const.SYNC_CALENDAR, false)
                && ContextCompat.checkSelfPermission(this, WRITE_CALENDAR) == PERMISSION_GRANTED
        val needsSync = syncManager.needSync(Const.SYNC_CALENDAR, TIME_TO_SYNC_CALENDAR)

        if (shouldSyncCalendar.not() || needsSync.not()) {
            return
        }

        try {
            calendarController.syncCalendar()
            syncManager.replaceIntoDb(Const.SYNC_CALENDAR)
        } catch (e: SQLiteException) {
            Utils.log(e)
        }
    }

    private fun createRoomLocationsOrNull(item: CalendarItem): RoomLocation? {
        val geo = tumLocationManager.roomLocationStringToGeo(item.location)
        return geo?.let {
            RoomLocation(item.location, it)
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
