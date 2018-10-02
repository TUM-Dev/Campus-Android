package de.tum.`in`.tumcampusapp.component.tumui

import android.app.SearchManager
import android.os.Bundle
import android.view.View
import android.widget.TextView
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.navigation.NavigationManager
import de.tum.`in`.tumcampusapp.component.other.navigation.SystemActivity
import de.tum.`in`.tumcampusapp.component.tumui.calendar.CalendarActivity
import de.tum.`in`.tumcampusapp.component.tumui.calendar.NextLectureCard
import de.tum.`in`.tumcampusapp.component.tumui.roomfinder.RoomFinderActivity
import de.tum.`in`.tumcampusapp.component.ui.overview.card.CardViewHolder
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.DateTimeUtils
import kotlinx.android.synthetic.main.card_next_lecture_item.view.*
import org.joda.time.format.DateTimeFormat
import java.util.*

class NextLectureCardViewHolder(itemView: View) : CardViewHolder(itemView) {

    fun bind(items: List<NextLectureCard.CardCalendarItem>) = with(itemView) {
        showLecture(items.first(), 0)

        /*
        if (items.size == 1) {
            linearLayout.visibility = View.GONE
            return
        }
        */

        IDS.forEachIndexed { index, buttonResId ->
            val button = findViewById<TextView>(buttonResId)
            if (index < items.size) {
                val lecture = items[index]
                button.setOnClickListener { showLecture(lecture, index) }
            } else {
                button.visibility = View.GONE
            }
        }
    }

    private fun showLecture(lecture: NextLectureCard.CardCalendarItem, index: Int) = with(itemView) {
        for (i in 0 until 4) {
            val button = findViewById<TextView>(IDS[i])
            button.isSelected = (i == index)
        }

        lectureTitleTextView.text = lecture.title
        lectureTimeTextView.text = DateTimeUtils.formatFutureTime(lecture.start, context)

        if (lecture.location == null || lecture.location.isEmpty()) {
            lectureLocationTextView.visibility = View.GONE
        } else {
            lectureLocationTextView.text = lecture.location
            lectureLocationTextView.setOnClickListener {
                val bundle = Bundle()
                bundle.putString(SearchManager.QUERY, lecture.locationForSearch)
                val destination = SystemActivity(RoomFinderActivity::class.java, bundle)
                NavigationManager.open(context, destination)
            }
        }

        val dayOfWeekFormatter = DateTimeFormat.forPattern("EEEE").withLocale(Locale.getDefault())
        val timeFormatter = DateTimeFormat.shortTime()

        val dayOfWeek = dayOfWeekFormatter.print(lecture.start)
        val startTime = timeFormatter.print(lecture.start)
        val endTime = timeFormatter.print(lecture.end)

        lectureEventTextView.text = String.format("%s, %s–%s", dayOfWeek, startTime, endTime)
        lectureEventTextView.setOnClickListener {
            val bundle = Bundle()
            bundle.putLong(Const.EVENT_TIME, lecture.start.millis)
            val destination = SystemActivity(CalendarActivity::class.java, bundle)
            NavigationManager.open(context, destination)
        }

    }

    companion object {

        private val IDS = intArrayOf(R.id.lecture_1, R.id.lecture_2, R.id.lecture_3, R.id.lecture_4)

    }

}
