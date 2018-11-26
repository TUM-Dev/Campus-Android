package de.tum.`in`.tumcampusapp.service

import android.Manifest
import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteException
import androidx.core.content.ContextCompat
import de.tum.`in`.tumcampusapp.component.other.locations.TumLocationManager
import de.tum.`in`.tumcampusapp.component.tumui.calendar.CalendarController
import de.tum.`in`.tumcampusapp.component.tumui.lectures.model.RoomLocation
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Utils
import de.tum.`in`.tumcampusapp.utils.injector
import de.tum.`in`.tumcampusapp.utils.sync.SyncManager
import org.jetbrains.anko.doAsync
import javax.inject.Inject

class QueryLocationsService : IntentService(QUERY_LOCATIONS) {

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

    override fun onHandleIntent(intent: Intent?) {
        doAsync {
            loadGeo()
        }
    }

    private fun loadGeo() {
        val calendarDao = database.calendarDao()
        val roomLocationsDao = database.roomLocationsDao()

        val calendarItems = calendarDao.lecturesWithoutCoordinates
        for (calendarItem in calendarItems) {
            val location = calendarItem.location
            if (location.isEmpty()) {
                continue
            }

            val geo = tumLocationManager.roomLocationStringToGeo(location)
            geo?.let {
                Utils.logv("inserted " + location + ' '.toString() + it)
                roomLocationsDao.insert(RoomLocation(location, it))
            }
        }

        // Do sync of google calendar if necessary
        val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR)
        val syncCalendar = Utils.getSettingBool(this, Const.SYNC_CALENDAR, false)
                && permission == PackageManager.PERMISSION_GRANTED

        if (!syncCalendar || !syncManager.needSync(Const.SYNC_CALENDAR, TIME_TO_SYNC_CALENDAR)) {
            return
        }

        try {
            calendarController.syncCalendar()
            syncManager.replaceIntoDb(Const.SYNC_CALENDAR)
        } catch (e: SQLiteException) {
            Utils.log(e)
        }
    }

    companion object {

        private const val QUERY_LOCATIONS = "query_locations"
        private const val TIME_TO_SYNC_CALENDAR = 604800 // 1 week

        @JvmStatic
        fun start(context: Context) {
            val intent = Intent(context, QueryLocationsService::class.java)
            context.startService(intent)
        }

    }

}
