package de.tum.`in`.tumcampusapp.component.tumui.calendar.model

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.api.tumonline.converters.DateTimeConverter
import de.tum.`in`.tumcampusapp.component.notifications.model.FutureNotification
import de.tum.`in`.tumcampusapp.component.notifications.persistence.NotificationType
import de.tum.`in`.tumcampusapp.component.other.locations.model.Geo
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.DateTimeUtils
import de.tum.`in`.tumcampusapp.utils.Utils
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.LocalDateTime

@Xml(name = "event")
data class Event(
    @PropertyElement(name = "description") val description: String? = null,
    @PropertyElement(name = "dtstart", converter = DateTimeConverter::class) val startTime: DateTime? = null,
    @PropertyElement(name = "dtend", converter = DateTimeConverter::class) val endTime: DateTime? = null,
    @PropertyElement(name = "geo") val geo: Geo? = null,
    @PropertyElement(name = "location") val location: String? = null,
    @PropertyElement(name = "nr") val id: String? = null,
    @PropertyElement(name = "status") val status: String? = null,
    @PropertyElement(name = "title") val title: String,
    @PropertyElement(name = "url") val url: String? = null
) {

    val isFutureEvent: Boolean
        get() = startTime?.isAfterNow ?: false

    private val tzGer = DateTimeZone.forID("Europe/Berlin")

    /**
     * Retrieve related values for calendar item as CalendarItem object
     */
    fun toCalendarItem(): CalendarItem {
        return CalendarItem(
                id ?: "", status ?: "", url ?: "", title,
                description ?: "", getStartTimeInDeviceTimezone() ?: DateTime(),
                getEndTimeInDeviceTimezone() ?: DateTime(), Utils.stripHtml(location ?: ""), false
        )
    }

    fun toNotification(context: Context): FutureNotification? {
        val startTimeInDeviceTimeZone = getStartTimeInDeviceTimezone()
        val endTimeInDeviceTimeZone = getStartTimeInDeviceTimezone()

        if (id == null || startTimeInDeviceTimeZone == null || endTimeInDeviceTimeZone == null) {
            return null
        }

        val timestamp = DateTimeUtils.formatFutureTime(startTimeInDeviceTimeZone, context)
        val duration = endTimeInDeviceTimeZone.millis - startTimeInDeviceTimeZone.millis

        val notification = NotificationCompat.Builder(context, Const.NOTIFICATION_CHANNEL_DEFAULT)
                .setContentTitle(title)
                .setContentText(timestamp)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_outline_event_24px)
                .setShowWhen(false)
                .setColor(ContextCompat.getColor(context, R.color.color_primary))
                .setTimeoutAfter(duration)
                .build()

        val notificationTime = startTimeInDeviceTimeZone.minusMinutes(15)
        return FutureNotification(NotificationType.CALENDAR, id.toInt(), notification, notificationTime)
    }

    /**
     * If the device is in a different timezone than the german one, this method can be used to
     * map the event startTime to the user's timezone
     *
     * @return The event's startTime in the user's current timezone
     */
    private fun getStartTimeInDeviceTimezone(): DateTime? {
        return startTime?.withZoneRetainFields(tzGer)?.withZone(DateTimeZone.getDefault())
    }

    /**
     * If the device is in a different timezone than the german one, this method can be used to
     * map the event endTime to the user's timezone
     *
     * @return The event's endTime in the user's current timezone
     */
    private fun getEndTimeInDeviceTimezone(): DateTime? {
        return endTime?.withZoneRetainFields(tzGer)?.withZone(DateTimeZone.getDefault())
    }
}
