package de.tum.`in`.tumcampusapp.models.tumcabe

import de.tum.`in`.tumcampusapp.auxiliary.Const

/**
 * @param id to associate the pictures with the message
 * @param topic either Const.FEEDBACK_TOPIC_GENERAL or Const.FEEDBACK_TOPIC_APP
 *          (-> determines the final recipient of the feedback)
 * @param message the actual feedback that the user typed in
 * @param email reply-to email
 * @param imageCount nr of images that will be sent after the feedback itself
 */
data class Feedback(
        var id: String = "", // to be able to match the pictures to the text message
        var topic: String = Const.FEEDBACK_TOPIC_GENERAL,
        var message: String = "",
        var email: String = "",
        var latitude: Double = 0.toDouble(),
        var longitude: Double = 0.toDouble(),
        var imageCount: Int = 0)