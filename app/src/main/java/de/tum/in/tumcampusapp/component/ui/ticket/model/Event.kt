package de.tum.`in`.tumcampusapp.component.ui.ticket.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.text.format.DateFormat
import com.google.gson.annotations.SerializedName
import de.tum.`in`.tumcampusapp.utils.readDateTime
import de.tum.`in`.tumcampusapp.utils.writeDateTime
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

/**
 * Event
 *
 * @param id      Event-ID
 * @param image   Image url e.g. http://www.tu-film.de/img/film/poster/Fack%20ju%20Ghte.jpg
 * @param title   Title
 * @param description Description
 * @param locality Locality
 * @param start   Date
 * @param end     Date
 * @param link    Url, e.g. http://www.in.tum.de
 */
@Entity(tableName = "events")
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
        @ColumnInfo(name = "start_time")
        var startTime: DateTime = DateTime(),
        @ColumnInfo(name = "end_time")
        var endTime: DateTime? = null,
        @ColumnInfo(name = "event_url")
        var eventUrl: String = ""
) : Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            DateTime(parcel.readLong()),
            parcel.readDateTime(),
            parcel.readString()) {
    }

    fun getFormattedStartDateTime(context: Context): String {
        val date = DateTimeFormat.mediumDate().print(startTime)
        val pattern = if (DateFormat.is24HourFormat(context)) "H:mm" else "h:mm aa"
        val time = DateTimeFormat.forPattern(pattern).print(startTime)
        return "$date, $time"
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(imageUrl)
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeString(locality)
        parcel.writeLong(startTime.millis)
        parcel.writeDateTime(endTime)
        parcel.writeString(eventUrl)
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