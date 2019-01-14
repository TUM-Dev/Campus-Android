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
        val newCourses = response.body()?.exams.orEmpty().map { it.course }
        val existingCourses = gradesStore.gradedCourses
        val diff = newCourses - existingCourses

        if (diff.size > NOTIFICATION_THRESHOLD) {
            // We assume that this is the first time the user's grades are fetched and stored. Since
            // this likely includes old grades, we don't display a notification.
            gradesStore.store(newCourses)
            return
        }

        if (diff.isNotEmpty()) {
            showGradesNotification(diff)
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

    companion object {
        private const val NOTIFICATION_THRESHOLD = 2
    }

}
