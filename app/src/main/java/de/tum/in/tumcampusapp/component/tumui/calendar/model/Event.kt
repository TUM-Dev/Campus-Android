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

    /**
     * Retrieve related values for calendar item as CalendarItem object
     */
    fun toCalendarItem(): CalendarItem {
        return CalendarItem(
                id ?: "", status ?: "", url ?: "", title,
                description ?: "", startTime ?: DateTime(),
                endTime ?: DateTime(), Utils.stripHtml(location ?: ""), false
        )
    }

    fun toNotification(context: Context): FutureNotification? {
        if (id == null || startTime == null || endTime == null) {
            return null
        }

        val timestamp = DateTimeUtils.formatFutureTime(startTime, context)
        val duration = endTime.millis - startTime.millis

        val notification = NotificationCompat.Builder(context, Const.NOTIFICATION_CHANNEL_DEFAULT)
                .setContentTitle(title)
                .setContentText(timestamp)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_outline_event_24px)
                .setShowWhen(false)
                .setColor(ContextCompat.getColor(context, R.color.color_primary))
                .setTimeoutAfter(duration)
                .build()

        val notificationTime = startTime.minusMinutes(15)
        return FutureNotification(NotificationType.CALENDAR, id.toInt(), notification, notificationTime)
    }
}
