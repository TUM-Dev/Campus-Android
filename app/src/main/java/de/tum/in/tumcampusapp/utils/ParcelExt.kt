package de.tum.`in`.tumcampusapp.utils

import android.os.Parcel
import org.joda.time.DateTime

fun Parcel.writeDateTime(dateTime: DateTime?) {
    writeLong(dateTime?.millis ?: -1)
}

fun Parcel.readDateTime(): DateTime? {
    val millis = readLong()
    return if (millis != -1L) DateTime(millis) else null
}