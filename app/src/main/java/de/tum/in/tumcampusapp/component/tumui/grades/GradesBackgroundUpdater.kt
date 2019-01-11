package de.tum.`in`.tumcampusapp.component.tumui.grades

import android.content.Context
import de.tum.`in`.tumcampusapp.api.tumonline.CacheControl
import de.tum.`in`.tumcampusapp.api.tumonline.TUMOnlineClient
import de.tum.`in`.tumcampusapp.component.notifications.NotificationScheduler
import de.tum.`in`.tumcampusapp.component.tumui.grades.model.ExamList
import de.tum.`in`.tumcampusapp.utils.Utils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class GradesBackgroundUpdater @Inject constructor(
        private val context: Context,
        private val tumOnlineClient: TUMOnlineClient,
        private val notificationScheduler: NotificationScheduler,
        private val gradesStore: GradesStore
) : Callback<ExamList> {

    fun fetchGradesAndNotifyIfNecessary() {
        tumOnlineClient.getGrades(CacheControl.BYPASS_CACHE).enqueue(this)
    }

    override fun onResponse(call: Call<ExamList>, response: Response<ExamList>) {
        val courses = response.body()?.exams.orEmpty().map { it.course }
        val state = gradesStore.state

        if (state.isFirstRefresh) {
            // On the first refresh, we store all downloaded grades in the empty GradesStore. We
            // can't know if any of these grades are "new", so we don't show a notification in this
            // case.
            gradesStore.storeGradedCourses(courses)
            return
        }

        val newGradedCourses = courses - state.existingGrades
        if (newGradedCourses.isNotEmpty()) {
            showGradesNotification(newGradedCourses)
        }
    }

    private fun showGradesNotification(newGrades: List<String>) {
        val provider = GradesNotificationProvider(context, newGrades)
        val notification = provider.buildNotification() ?: return
        notificationScheduler.schedule(notification)
    }

    override fun onFailure(call: Call<ExamList>, t: Throwable) {
        Utils.log(t)
    }

}
