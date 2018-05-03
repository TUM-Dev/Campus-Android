package de.tum.`in`.tumcampusapp.service

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.provider.Settings
import android.support.annotation.RequiresApi
import android.support.v4.app.JobIntentService
import de.tum.`in`.tumcampusapp.component.tumui.calendar.CalendarController
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Const.SILENCE_SERVICE_JOB_ID
import de.tum.`in`.tumcampusapp.utils.Utils
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Service used to silence the mobile during lectures
 */
class SilenceService : JobIntentService() {

    /**
     * We can't and won't change the ringer modes, if the device is in DoNotDisturb mode. DnD requires
     * explicit user interaction, so we are out of the game until DnD is off again
     */

    // See: https://stackoverflow.com/questions/31387137/android-detect-do-not-disturb-status
    // Settings.System.getInt(getContentResolver(), Settings.System.DO_NOT_DISTURB, 1);

    private val isDoNotDisturbActive: Boolean
        get() {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                notificationManager.currentInterruptionFilter != android.app.NotificationManager.INTERRUPTION_FILTER_ALL
            } else {
                try {
                    val mode = Settings.Global.getInt(contentResolver, "zen_mode")
                    mode != 0
                } catch (e: Settings.SettingNotFoundException) {
                    false
                }
            }
        }

    override fun onCreate() {
        super.onCreate()
        Utils.log("SilenceService has started")
    }

    override fun onDestroy() {
        super.onDestroy()
        Utils.log("SilenceService has stopped")
    }

    override fun onHandleWork(intent: Intent) {
        //Abort, if the settingsPrefix changed
        if (!Utils.getSettingBool(this, Const.SILENCE_SERVICE, false)) {
            // Don't schedule a new run, since the service is disabled
            return
        }

        if (!hasPermissions(this)) {
            Utils.setSetting(this, Const.SILENCE_SERVICE, false)
            return
        }

        val alarmManager = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val newIntent = Intent(this, SilenceService::class.java)
        val pendingIntent = PendingIntent.getService(this, 0, newIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val startTime = System.currentTimeMillis()
        var waitDuration = CHECK_INTERVAL.toLong()
        Utils.log("SilenceService enabled, checking for lectures …")

        val calendarController = CalendarController(this)
        if (!calendarController.hasLectures()) {
            Utils.logv("No lectures available")
            alarmManager.set(AlarmManager.RTC, startTime + waitDuration, pendingIntent)
            return
        }

        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val currentLectures = calendarController.currentFromDb
        Utils.log("Current lectures: " + currentLectures.size)

        if (currentLectures.isEmpty() || isDoNotDisturbActive) {
            if (Utils.getSettingBool(this, Const.SILENCE_ON, false) && !isDoNotDisturbActive) {
                // default: old state
                Utils.log("set ringer mode to old state")
                val ringerMode = Utils.getSetting(this, Const.SILENCE_OLD_STATE, AudioManager.RINGER_MODE_NORMAL.toString())
                audioManager.ringerMode = ringerMode.toInt()
                Utils.setSetting(this, Const.SILENCE_ON, false)

                val nextCalendarItems = calendarController.nextCalendarItems

                // Check if we have a "next" item in the database and
                // update the refresh interval until then. Otherwise use default interval.
                if (!nextCalendarItems.isEmpty()) {
                    // refresh when next event has started
                    waitDuration = getWaitDuration(nextCalendarItems[0].dtstart)
                }
            }
        } else {
            // remember old state if just activated ; in doubt dont change
            if (!Utils.getSettingBool(this, Const.SILENCE_ON, true)) {
                Utils.setSetting(this, Const.SILENCE_OLD_STATE, audioManager.ringerMode)
            }

            // if current lecture(s) found, silence the mobile
            Utils.setSetting(this, Const.SILENCE_ON, true)

            // Set into silent or vibrate mode based on current setting
            val mode = Utils.getSetting(this, "silent_mode_set_to", "0")
            audioManager.ringerMode = when (mode) {
                RINGER_MODE_SILENT -> AudioManager.RINGER_MODE_VIBRATE
                else -> AudioManager.RINGER_MODE_SILENT
            }

            // refresh when event has ended
            waitDuration = getWaitDuration(currentLectures[0].dtstart)
        }

        alarmManager.set(AlarmManager.RTC, startTime + waitDuration, pendingIntent)
    }

    companion object {

        /**
         * Interval in milliseconds to check for current lectures
         */
        private const val CHECK_INTERVAL = 60000 * 15 // 15 Minutes
        private const val CHECK_DELAY = 10000 // 10 Seconds after Calendar changed

        private const val RINGER_MODE_SILENT = "0"

        private fun getWaitDuration(timeToEventString: String): Long {
            var timeToEvent = java.lang.Long.MAX_VALUE
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH)
                timeToEvent = sdf.parse(timeToEventString).time
            } catch (e: ParseException) {
                Utils.log(e)
            }

            return Math.min(CHECK_INTERVAL.toLong(), timeToEvent - System.currentTimeMillis() + CHECK_DELAY)
        }

        @JvmStatic fun enqueueWork(context: Context, work: Intent) {
            enqueueWork(context, SilenceService::class.java, SILENCE_SERVICE_JOB_ID, work)
        }

        /**
         * Check if the app has the permissions to enable "Do Not Disturb".
         */
        fun hasPermissions(context: Context): Boolean {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            return !(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !notificationManager.isNotificationPolicyAccessGranted)
        }

        /**
         * Request the "Do Not Disturb" permissions for android version >= N.
         */
        fun requestPermissions(context: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                return
            }
            if (hasPermissions(context)) {
                return
            }
            requestPermissionsSDK23(context)
        }

        @RequiresApi(Build.VERSION_CODES.M)
        private fun requestPermissionsSDK23(context: Context) {
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            context.startActivity(intent)
        }

    }

}
