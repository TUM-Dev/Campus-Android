package de.tum.`in`.tumcampusapp.utils

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import de.tum.`in`.tumcampusapp.api.tumonline.CacheControl
import de.tum.`in`.tumcampusapp.api.tumonline.TUMOnlineClient
import de.tum.`in`.tumcampusapp.component.tumui.calendar.CalendarController
import de.tum.`in`.tumcampusapp.component.tumui.calendar.model.EventsResponse
import de.tum.`in`.tumcampusapp.service.QueryLocationsService
import okhttp3.Cache
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class CacheManager @Inject constructor(private val context: Context) {

    val cache: Cache
        get() = Cache(context.cacheDir, 10 * 1024 * 1024) // 10 MB

    fun fillCache() {
        class WorkWhenReceived(appContext: Context, workerParams: WorkerParameters) :
                Worker(appContext, workerParams) {
            override fun doWork(): Result {
                syncCalendar()
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

    private fun syncCalendar() {
        TUMOnlineClient
                .getInstance(context)
                .getCalendar(CacheControl.USE_CACHE)
                .enqueue(object : Callback<EventsResponse> {
                    override fun onResponse(call: Call<EventsResponse>, response: Response<EventsResponse>) {
                        val eventsResponse = response.body() ?: return
                        val events = eventsResponse.events ?: return
                        CalendarController(context).importCalendar(events)
                        loadRoomLocations()
                    }

                    override fun onFailure(call: Call<EventsResponse>, t: Throwable) {
                        Utils.log(t, "Error while loading calendar in CacheManager")
                    }
                })
    }

    private fun loadRoomLocations() {
        // enqueues OneTimeWorkRequest
        QueryLocationsService.enqueueWork(context)
    }

    @Synchronized
    fun clearCache() {
        cache.delete()
    }
}