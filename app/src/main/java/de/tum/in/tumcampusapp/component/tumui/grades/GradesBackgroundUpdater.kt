package de.tum.`in`.tumcampusapp.component.tumui.grades

import android.content.Context
import de.tum.`in`.tumcampusapp.api.tumonline.CacheControl
import de.tum.`in`.tumcampusapp.api.tumonline.TUMOnlineClient
import de.tum.`in`.tumcampusapp.component.notifications.NotificationScheduler
import de.tum.`in`.tumcampusapp.component.tumui.grades.model.Exam
import de.tum.`in`.tumcampusapp.component.tumui.grades.model.ExamList
import de.tum.`in`.tumcampusapp.utils.Utils
import org.joda.time.DateTime
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

    fun fetchGradesAndNotifyIfNew(forceReload: Boolean) {
        val cacheControl = if (forceReload) CacheControl.BYPASS_CACHE else CacheControl.USE_CACHE
        tumOnlineClient.getGrades(cacheControl).enqueue(this)
    }

    override fun onResponse(call: Call<ExamList>, response: Response<ExamList>) {
        val exams = response.body()?.exams.orEmpty()
        val courses = exams.map { it.course }
        val onlyOldExams = isLikelyOldExams(exams)

        if (onlyOldExams) {
            return
        }

        val existingCourses = gradesStore.getGradedCourses()
        val newGradedCourses = existingCourses - courses

        if (newGradedCourses.isNotEmpty()) {
            showGradesNotification(newGradedCourses)
        }
    }

    private fun isLikelyOldExams(exams: List<Exam>): Boolean {
        // The grade is of an exam that occurred more than 4 weeks ago. To not notify the user
        // the first time that they use this feature, we don't show a notification in this case.
        val newestExam = exams
                .mapNotNull { it.date }
                .max() ?: DateTime.now()
        return newestExam.isBefore(EXAM_THRESHOLD)
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
        private val EXAM_THRESHOLD = DateTime.now().minusWeeks(4)
    }

}
