package de.tum.`in`.tumcampusapp.component.tumui.feedback.model

import android.location.Location
import android.os.Build
import android.os.Parcelable
import de.tum.`in`.tumcampusapp.BuildConfig
import de.tum.`in`.tumcampusapp.utils.Const
import kotlinx.android.parcel.Parcelize
import java.util.UUID

/**
 * @param id to associate the pictures with the message
 * @param topic either Const.FEEDBACK_TOPIC_GENERAL or Const.FEEDBACK_TOPIC_APP
 *          (-> determines the final recipient of the feedback)
 * @param message the actual feedback that the user typed in
 * @param email reply-to email
 * @param imageCount needed for the server (has to know for how many images it has to wait)
 * @param osVersion helpful info if bugs are submitted
 * @param appVersion helpful info if bugs are submitted
 */
@Parcelize
data class Feedback(
    val id: String = UUID.randomUUID().toString(),
    var topic: String = Const.FEEDBACK_TOPIC_GENERAL,
    var message: String? = null,
    var email: String? = null,
    var includeEmail: Boolean = false,
    var includeLocation: Boolean = false,
    var latitude: Double? = null,
    var longitude: Double? = null,
    val osVersion: String = Build.VERSION.RELEASE,
    val appVersion: String = BuildConfig.VERSION_NAME,
    var imageCount: Int = 0,
    @Transient // don't send this
    var picturePaths: MutableList<String> = mutableListOf()
) : Parcelable {

    var location: Location?
        get() {
            val lat = latitude ?: return null
            val lng = longitude ?: return null
            return Location("").apply {
                latitude = lat
                longitude = lng
            }
        }
        set(value) {
            latitude = value?.latitude
            longitude = value?.longitude
        }
}
