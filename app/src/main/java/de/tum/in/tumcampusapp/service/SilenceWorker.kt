package de.tum.`in`.tumcampusapp.service

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import de.tum.`in`.tumcampusapp.component.tumui.calendar.CalendarController
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Utils
import org.joda.time.DateTime

class SilenceWorker(appContext: Context, workerParams: WorkerParameters):
        Worker(appContext, workerParams) {

    /**
     * We can't and won't change the ringer modes, if the device is in DoNotDisturb mode. DnD requires
     * explicit user interaction, so we are out of the game until DnD is off again
     */


    // See: https://stackoverflow.com/questions/31387137/android-detect-do-not-disturb-status
    // Settings.System.getInt(getContentResolver(), Settings.System.DO_NOT_DISTURB, 1);

    private val isDoNotDisturbActive: Boolean
        get() {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL
            } else {
                try {
                    val mode = Settings.Global.getInt(applicationContext.contentResolver, "zen_mode")
                    mode != 0
                } catch (e: Settings.SettingNotFoundException) {
                    false
                }
            }
        }

    override fun doWork(): Result {
        Utils.log("Silence service worker started …")
        // Abort, if the settingsPrefix changed
        if (!Utils.getSettingBool(applicationContext, Const.SILENCE_SERVICE, false)) {
            // Don't schedule a new run, since the service is disabled
            return Result.success()
        }

        if (!hasPermissions(applicationContext)) {
            Utils.setSetting(applicationContext, Const.SILENCE_SERVICE, false)
            return Result.failure()
        }

        val alarmManager = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val newIntent = Intent(applicationContext, SilenceWorker::class.java)
        val pendingIntent = PendingIntent.getService(applicationContext, 0, newIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val startTime = System.currentTimeMillis()
        var waitDuration = CHECK_INTERVAL.toLong()
        Utils.log("SilenceService enabled, checking for lectures …")

        val calendarController = CalendarController(applicationContext)
        if (!calendarController.hasLectures()) {
            Utils.logVerbose("No lectures available")
            alarmManager.set(AlarmManager.RTC, startTime + waitDuration, pendingIntent)
            return Result.success()
        }

        val audioManager = applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val currentLectures = calendarController.currentLectures
        Utils.log("Current lectures: " + currentLectures.size)

        if (currentLectures.isEmpty() || isDoNotDisturbActive) {
            if (Utils.getSettingBool(applicationContext, Const.SILENCE_ON, false) && !isDoNotDisturbActive) {
                // default: old state
                Utils.log("set ringer mode to old state")
                val ringerMode = Utils.getSetting(applicationContext, Const.SILENCE_OLD_STATE, AudioManager.RINGER_MODE_NORMAL.toString())
                audioManager.ringerMode = ringerMode.toInt()
                Utils.setSetting(applicationContext, Const.SILENCE_ON, false)

                val nextCalendarItems = calendarController.nextCalendarItems

                // Check if we have a "next" item in the database and
                // update the refresh interval until then. Otherwise use default interval.
                if (nextCalendarItems.isNotEmpty()) {
                    // refresh when next event has started
                    waitDuration = getWaitDuration(nextCalendarItems[0].dtstart)
                }
            }
        } else {
            // remember old state if just activated ; in doubt dont change
            if (!Utils.getSettingBool(applicationContext, Const.SILENCE_ON, true)) {
                Utils.setSetting(applicationContext, Const.SILENCE_OLD_STATE, audioManager.ringerMode)
            }

            // if current lecture(s) found, silence the mobile
            Utils.setSetting(applicationContext, Const.SILENCE_ON, true)

            // Set into silent or vibrate mode based on current setting
            val mode = Utils.getSetting(applicationContext, "silent_mode_set_to", "0")
            audioManager.ringerMode = when (mode) {
                RINGER_MODE_SILENT -> AudioManager.RINGER_MODE_VIBRATE
                else -> AudioManager.RINGER_MODE_SILENT
            }

            // refresh when event has ended
            waitDuration = getWaitDuration(currentLectures[0].dtstart)
        }

        alarmManager.set(AlarmManager.RTC, startTime + waitDuration, pendingIntent)
        return Result.success()
    }
    companion object {

        /**
         * Interval in milliseconds to check for current lectures
         */
        private const val CHECK_INTERVAL = 60000 * 15 // 15 Minutes
        private const val CHECK_DELAY = 10000 // 10 Seconds after Calendar changed

        private const val RINGER_MODE_SILENT = "0"

        private const val UNIQUE_WORK_NAME = "SilenceWorker_request"
        private fun getWaitDuration(eventDateTime: DateTime): Long {
            val eventTime = eventDateTime.millis
            return Math.min(CHECK_INTERVAL.toLong(), eventTime - System.currentTimeMillis() + CHECK_DELAY)
        }

        @JvmStatic fun enqueueWork(context: Context) {
            val workManager = WorkManager.getInstance(context)
            val silenceWorkRequest = OneTimeWorkRequestBuilder<SilenceWorker>().build()
            //only allow one silence worker work to be available. and keep it if another one is created.
            workManager.enqueueUniqueWork(UNIQUE_WORK_NAME, ExistingWorkPolicy.KEEP, silenceWorkRequest)
        }

        @JvmStatic fun dequeueWork(context: Context){
            val workManager = WorkManager.getInstance(context)
            //we dequeue the work with its unique name
            workManager.cancelUniqueWork(UNIQUE_WORK_NAME)
        }

        /**
         * Check if the app has the permissions to enable "Do Not Disturb".
         */
        @JvmStatic fun hasPermissions(context: Context): Boolean {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            return !(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !notificationManager.isNotificationPolicyAccessGranted)
        }

        /**
         * Request the "Do Not Disturb" permissions for android version >= N.
         */
        @JvmStatic fun requestPermissions(context: Context) {
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