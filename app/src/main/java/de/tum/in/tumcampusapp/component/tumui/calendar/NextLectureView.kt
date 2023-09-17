package de.tum.`in`.tumcampusapp.component.tumui.calendar

import android.content.Context
import android.text.format.DateUtils.DAY_IN_MILLIS
import android.text.format.DateUtils.FORMAT_ABBREV_RELATIVE
import android.text.format.DateUtils.HOUR_IN_MILLIS
import android.text.format.DateUtils.MINUTE_IN_MILLIS
import android.text.format.DateUtils.getRelativeTimeSpanString
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import de.tum.`in`.tumcampusapp.R
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import java.util.*

class NextLectureView
@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr) {

    private val view = LayoutInflater.from(context).inflate(R.layout.layout_card_lecture, this, true)

    fun setLecture(lecture: NextLectureCard.CardCalendarItem) {
        val lectureLocationTextView = view.findViewById<TextView>(R.id.lectureLocationTextView)
        val lectureTimeTextView = view.findViewById<TextView>(R.id.lectureTimeTextView)
        val lectureTitleTextView = view.findViewById<TextView>(R.id.lectureTitleTextView)

        lectureTitleTextView.text = lecture.title
        lectureTimeTextView.text = formatLectureDate(lecture.start, lecture.end, context)

        if (lecture.locations.isNullOrEmpty()) {
            lectureLocationTextView.visibility = View.GONE
        } else {
            lectureLocationTextView.visibility = View.VISIBLE
            lectureLocationTextView.text = lecture.locationString
        }

        view.setOnClickListener { openEventBottomSheet(lecture) }
    }

    /**
     * Format a recently started or future date.
     * Examples:
     * - "1 h 20 m left"
     * - "20 minutes left"
     * - "Starts now"
     * - "In 32 minutes"
     * - "Today 18:30"
     * - "Tomorrow 08:30"
     * - "In 4 days"
     */
    private fun formatLectureDate(startTime: DateTime, endTime: DateTime, context: Context): String {
        val timeInMillis = startTime.millis
        val now = DateTime.now()

        val diff = timeInMillis - now.millis

        return when {
            diff < 0 -> {
                val timeToEnd = endTime.millis - now.millis
                val formatter = DateTimeFormat.forPattern(
                    if (timeToEnd < HOUR_IN_MILLIS) {
                        "m 'min'"
                    } else {
                        "h 'h' m 'min'"
                    }
                ).withLocale(Locale.ENGLISH)
                val readableTimeLeft = formatter.print(DateTime(timeToEnd, DateTimeZone.UTC))
                context.getString(R.string.ongoing_until, readableTimeLeft)
            }
            diff < MINUTE_IN_MILLIS -> {
                context.getString(R.string.starts_now)
            }
            diff < 5 * HOUR_IN_MILLIS -> {
                val formatter = DateTimeFormat.forPattern(
                    if (diff < HOUR_IN_MILLIS) {
                        "m 'min'"
                    } else {
                        "h 'h' m 'min'"
                    }
                ).withLocale(Locale.ENGLISH)
                "${context.getString(R.string.IN_capitalized)} ${formatter.print(DateTime(diff, DateTimeZone.UTC))}"
            }
            // Today
            startTime.dayOfYear() == now.dayOfYear() && startTime.year() == now.year() -> {
                DateTimeFormat.forPattern("HH:mm").withLocale(Locale.ENGLISH).print(startTime)
            }
            // Tomorrow
            now.withDurationAdded(DAY_IN_MILLIS, 1).let { tomorrow ->
                startTime.dayOfYear() == tomorrow.dayOfYear() && startTime.year() == tomorrow.year()
            } -> {
                val timeStr = DateTimeFormat.forPattern("HH:mm").withLocale(Locale.ENGLISH).print(startTime)
                context.getString(R.string.tomorrow_time, timeStr)
            }
            else -> getRelativeTimeSpanString(timeInMillis, now.millis, MINUTE_IN_MILLIS, FORMAT_ABBREV_RELATIVE).toString()
        }
    }

    private fun openEventBottomSheet(item: NextLectureCard.CardCalendarItem) {
        val detailsFragment = CalendarDetailsFragment.newInstance(item.id, isShownInCalendarActivity = false)
        val activity = context as AppCompatActivity
        detailsFragment.show(activity.supportFragmentManager, null)
    }
}
