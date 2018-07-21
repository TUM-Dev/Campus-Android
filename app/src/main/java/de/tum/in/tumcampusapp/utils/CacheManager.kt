package de.tum.`in`.tumcampusapp.utils

import android.content.Context
import de.tum.`in`.tumcampusapp.api.tumonline.AccessTokenManager
import de.tum.`in`.tumcampusapp.api.tumonline.CacheControl
import de.tum.`in`.tumcampusapp.api.tumonline.TUMOnlineClient
import de.tum.`in`.tumcampusapp.component.tumui.lectures.model.LecturesResponse
import de.tum.`in`.tumcampusapp.component.ui.chat.ChatRoomController
import okhttp3.Cache
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class CacheManager(private val context: Context) {

    val cache: Cache
        get() = Cache(context.cacheDir, 10 * 1024 * 1024) // 10 MB

    fun fillCache() {
        if (!AccessTokenManager.hasValidAccessToken(context)) {
            return
        }

        syncCalendar()
        syncPersonalLectures()
    }

    fun syncCalendar() {
        try {
            TUMOnlineClient
                    .getInstance(context)
                    .getCalendar(CacheControl.USE_CACHE)
                    .execute()
        } catch (e: IOException) {
            Utils.log(e, "Error while loading calendar in CacheManager")
        }
    }

    private fun syncPersonalLectures() {
        TUMOnlineClient
                .getInstance(context)
                .getPersonalLectures(CacheControl.USE_CACHE)
                .enqueue(object : Callback<LecturesResponse> {
                    override fun onResponse(call: Call<LecturesResponse>,
                                            response: Response<LecturesResponse>) {
                        Utils.log("Successfully updated personal lectures in backround")
                        val lectures = response.body()?.lectures ?: return
                        val chatRoomController = ChatRoomController(context)
                        chatRoomController.createLectureRooms(lectures)
                    }

                    override fun onFailure(call: Call<LecturesResponse>, t: Throwable) {
                        Utils.log(t, "Error loading personal lectures in backround")
                    }
                })
    }

    @Synchronized
    fun clearCache() {
        cache.delete()
    }

}