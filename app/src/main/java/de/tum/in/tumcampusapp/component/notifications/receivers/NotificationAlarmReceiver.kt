package de.tum.`in`.tumcampusapp.component.notifications.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import de.tum.`in`.tumcampusapp.component.notifications.NotificationScheduler
import de.tum.`in`.tumcampusapp.component.notifications.persistence.NotificationType
import de.tum.`in`.tumcampusapp.component.tumui.tutionfees.TuitionFeesNotificationProvider
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.CafeteriaNotificationProvider
import de.tum.`in`.tumcampusapp.component.ui.transportation.TransportNotificationProvider
import de.tum.`in`.tumcampusapp.utils.Const

class NotificationAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val typeId = intent.getLongExtra(Const.KEY_NOTIFICATION_TYPE_ID, 0)

        val notificationProvider = when (NotificationType.fromId(typeId)) {
            NotificationType.CAFETERIA -> CafeteriaNotificationProvider(context)
            NotificationType.TRANSPORT -> TransportNotificationProvider(context)
            NotificationType.TUITION_FEES -> TuitionFeesNotificationProvider(context)
            else -> return
        }

        // create subclass of Worker to enqueue with WorkManager
        class WorkWhenReceived(appContext: Context, workerParams: WorkerParameters) :
                Worker(appContext, workerParams) {
            override fun doWork(): Result {
                val notification = notificationProvider.buildNotification()
                notification?.let {
                    NotificationScheduler(applicationContext).schedule(it)
                }
                return Result.success()
            }
        }
        // start expedited background work
        val request = OneTimeWorkRequestBuilder<WorkWhenReceived>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()
        WorkManager.getInstance(context)
                .enqueue(request)
    }
}
