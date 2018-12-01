package de.tum.`in`.tumcampusapp.component.tumui.feedback.model

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import de.tum.`in`.tumcampusapp.BuildConfig
import de.tum.`in`.tumcampusapp.utils.Const
import java.util.*
import kotlin.collections.ArrayList

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
data class Feedback(
        val id: String = UUID.randomUUID().toString(),
        var topic: String = Const.FEEDBACK_TOPIC_GENERAL,
        var message: String = "",
        var email: String = "",
        var includeEmail: Boolean = false,
        var includeLocation: Boolean = false,
        var latitude: Double = 0.toDouble(),
        var longitude: Double = 0.toDouble(),
        val osVersion: String = Build.VERSION.RELEASE,
        val appVersion: String = BuildConfig.VERSION_NAME,
        var imageCount: Int = 0,
        @Transient // don't send this
        var picturePaths: List<String> = ArrayList()): Parcelable {

    constructor(parcel: Parcel) : this() {
        topic = parcel.readString()!!
        message = parcel.readString()!!
        email = parcel.readString()!!
        includeEmail = parcel.readInt() == 1
        includeLocation = parcel.readInt() == 1
        latitude = parcel.readDouble()
        longitude = parcel.readDouble()
        imageCount = parcel.readInt()
        parcel.readStringList(picturePaths)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(topic)
        parcel.writeString(message)
        parcel.writeString(email)
        parcel.writeInt(if (includeEmail) 1 else 0)
        parcel.writeInt(if (includeLocation) 1 else 0)
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
        parcel.writeInt(imageCount)
        parcel.writeStringList(picturePaths)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<Feedback> {
        override fun createFromParcel(parcel: Parcel): Feedback {
            return Feedback(parcel)
        }

        override fun newArray(size: Int): Array<Feedback?> {
            return arrayOfNulls(size)
        }
    }
}