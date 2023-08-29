package de.tum.`in`.tumcampusapp.utils

import android.content.Context
import de.tum.`in`.tumcampusapp.api.tumonline.CacheControl
import de.tum.`in`.tumcampusapp.api.tumonline.TUMOnlineClient
import de.tum.`in`.tumcampusapp.component.tumui.calendar.CalendarController
import de.tum.`in`.tumcampusapp.component.tumui.calendar.model.EventsResponse
import de.tum.`in`.tumcampusapp.service.QueryLocationWorker
import okhttp3.Cache
import org.jetbrains.anko.doAsync
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class CacheManager @Inject constructor(private val context: Context) {

    val cache: Cache
        get() = Cache(context.cacheDir, 10 * 1024 * 1024) // 10 MB

    fun fillCache() {
        doAsync {
            syncCalendar()
        }
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
        doAsync {
            QueryLocationWorker.enqueueWork(context)
        }
    }

    @Synchronized
    fun clearCache() {
        cache.delete()
    }
}
