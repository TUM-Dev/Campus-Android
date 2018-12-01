package de.tum.`in`.tumcampusapp.utils

import android.content.Context
import de.tum.`in`.tumcampusapp.api.tumonline.CacheControl
import de.tum.`in`.tumcampusapp.api.tumonline.TUMOnlineClient
import de.tum.`in`.tumcampusapp.component.tumui.calendar.CalendarController
import de.tum.`in`.tumcampusapp.component.tumui.calendar.model.EventsResponse
import de.tum.`in`.tumcampusapp.component.tumui.lectures.model.LecturesResponse
import de.tum.`in`.tumcampusapp.component.ui.chat.ChatRoomController
import de.tum.`in`.tumcampusapp.service.QueryLocationsService
import org.jetbrains.anko.doAsync
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class BackgroundUpdater @Inject constructor(
        private val context: Context,
        private val tumOnlineClient: TUMOnlineClient,
        private val calendarController: CalendarController,
        private val chatRoomController: ChatRoomController
) {

    fun update() {
        doAsync {
            syncCalendar()
            syncPersonalLectures()
        }
    }

    private fun syncCalendar() {
        tumOnlineClient.getCalendar(CacheControl.USE_CACHE)
                .enqueue(object : Callback<EventsResponse> {
                    override fun onResponse(call: Call<EventsResponse>,
                                            response: Response<EventsResponse>) {
                        val eventsResponse = response.body() ?: return
                        val events = eventsResponse.events ?: return
                        calendarController.importCalendar(events)
                        loadRoomLocations()
                    }

                    override fun onFailure(call: Call<EventsResponse>, t: Throwable) {
                        Utils.log(t, "Error while loading calendar in CacheManager")
                    }
                })
    }

    private fun loadRoomLocations() {
        doAsync {
            QueryLocationsService.enqueueWork(context)
        }
    }

    private fun syncPersonalLectures() {
        tumOnlineClient
                .getPersonalLectures(CacheControl.USE_CACHE)
                .enqueue(object : Callback<LecturesResponse> {
                    override fun onResponse(call: Call<LecturesResponse>,
                                            response: Response<LecturesResponse>) {
                        Utils.log("Successfully updated personal lectures in background")
                        val lectures = response.body()?.lectures ?: return
                        chatRoomController.createLectureRooms(lectures)
                    }

                    override fun onFailure(call: Call<LecturesResponse>, t: Throwable) {
                        Utils.log(t, "Error loading personal lectures in background")
                    }
                })
    }

}
