package de.tum.`in`.tumcampusapp.component.tumui.feedback.model

import android.os.Build
import de.tum.`in`.tumcampusapp.BuildConfig
import de.tum.`in`.tumcampusapp.component.tumui.feedback.FeedbackController
import de.tum.`in`.tumcampusapp.utils.Const
import java.util.*

/**
 * @param id to associate the pictures with the message
 * @param topic either Const.FEEDBACK_TOPIC_GENERAL or Const.FEEDBACK_TOPIC_APP
 *          (-> determines the final recipient of the feedback)
 * @param message the actual feedback that the user typed in
 * @param email reply-to email
 * @param osVersion helpful info if bugs are submitted
 * @param appVersion helpful info if bugs are submitted
 */
data class Feedback(
        val id: String = UUID.randomUUID().toString(), // to be able to match the pictures to the text message
        var topic: String = Const.FEEDBACK_TOPIC_GENERAL,
        var message: String = "",
        var email: String = "",
        var includeEmail: Boolean = false,
        var latitude: Double = 0.toDouble(),
        var longitude: Double = 0.toDouble(),
        var osVersion: String = Build.VERSION.RELEASE,
        var appVersion: String = BuildConfig.VERSION_NAME,
        var picturePaths: List<String> = ArrayList()) {

    fun setTopic(topicId: Int) {
        if (topicId == FeedbackController.GENERAL_FEEDBACK) {
            topic = Const.FEEDBACK_TOPIC_GENERAL
        } else {
            topic = Const.FEEDBACK_TOPIC_APP
        }
    }
}