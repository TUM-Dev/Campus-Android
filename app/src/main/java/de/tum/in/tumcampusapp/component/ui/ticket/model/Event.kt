package de.tum.`in`.tumcampusapp.component.ui.ticket.model

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.text.format.DateFormat
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.RoomWarnings
import com.google.gson.annotations.SerializedName
import de.tum.`in`.tumcampusapp.utils.readDateTime
import de.tum.`in`.tumcampusapp.utils.writeDateTime
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

/**
 * Event
 *
 * @param id          Event-ID
 * @param imageUrl    Image url e.g. http://www.tu-film.de/img/film/poster/Fack%20ju%20Ghte.jpg
 * @param title       Title
 * @param description Description
 * @param locality    Locality
 * @param startTime   DateTime
 * @param endTime     DateTime
 * @param eventUrl    Url, e.g. http://www.in.tum.de
 */
@Entity(tableName = "events")
@SuppressWarnings(RoomWarnings.DEFAULT_CONSTRUCTOR)
data class Event(
        @PrimaryKey
        @SerializedName("event")
        var id: Int = 0,
        @SerializedName("file")
        @ColumnInfo(name = "image_url")
        var imageUrl: String? = null,
        var title: String = "",
        var description: String = "",
        var locality: String = "",
        @SerializedName("start")
        @ColumnInfo(name = "start_time")
        var startTime: DateTime = DateTime(),
        @SerializedName("end")
        @ColumnInfo(name = "end_time")
        var endTime: DateTime? = null,
        @SerializedName("link")
        @ColumnInfo(name = "event_url")
        var eventUrl: String = "",
        @ColumnInfo(name = "dismissed")
        var dismissed: Int = 0,
        var kino: Int = -1,
        var news: Int = -1
) : Parcelable, Comparable<Event> {

    // Unsafe calls are only ok because we control writeToParcel().
    // Keep in sync with writeToParcel()!
    constructor(parcel: Parcel) : this(
            id = parcel.readInt(),
            imageUrl = parcel.readString(),
            title = parcel.readString()!!,
            description = parcel.readString()!!,
            locality = parcel.readString()!!,
            startTime = DateTime(parcel.readLong()),
            endTime = parcel.readDateTime(),
            eventUrl = parcel.readString()!!,
            dismissed = parcel.readInt(),
            kino = parcel.readInt(),
            news = parcel.readInt())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(imageUrl)
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeString(locality)
        parcel.writeLong(startTime.millis)
        parcel.writeDateTime(endTime)
        parcel.writeString(eventUrl)
        parcel.writeInt(dismissed)
        parcel.writeInt(kino)
        parcel.writeInt(news)
    }

    fun getFormattedStartDateTime(context: Context): String {
        val date = DateTimeFormat.longDate().print(startTime)
        val pattern = if (DateFormat.is24HourFormat(context)) "H:mm" else "h:mm aa"
        val time = DateTimeFormat.forPattern(pattern).print(startTime)
        return "$date, $time"
    }

    override fun compareTo(other: Event): Int {
        return startTime.compareTo(other.startTime)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {

        const val defaultDuration = 7200000 // Milliseconds

        @JvmField var CREATOR = object : Parcelable.Creator<Event> {
            override fun createFromParcel(parcel: Parcel) = Event(parcel)

            override fun newArray(size: Int) = arrayOfNulls<Event?>(size)
        }

    }

}