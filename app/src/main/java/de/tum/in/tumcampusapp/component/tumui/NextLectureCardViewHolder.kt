package de.tum.`in`.tumcampusapp.component.tumui

import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
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
        //card_title
        showLecture(items.first(), 0)

        IDS.forEachIndexed { index, buttonResId ->
            val button = findViewById<Button>(buttonResId)
            if (index < items.size && items.size > 1) {
                val lecture = items[index]
                button.setOnClickListener { showLecture(lecture, index) }
            } else {
                button.visibility = View.GONE
            }
        }
    }

    private fun showLecture(lecture: NextLectureCard.CardCalendarItem, index: Int) = with(itemView) {
        for (i in 0 until 4) {
            findViewById<Button>(IDS[i]).isSelected = (i == index)
        }

        card_title.text = lecture.title
        card_time.text = DateTimeUtils.formatFutureTime(lecture.start, context)

        if (lecture.location == null || lecture.location.isEmpty()) {
            card_location_action.visibility = View.GONE
        } else {
            card_location_action.text = lecture.location
            card_location_action.setOnClickListener {
                val bundle = Bundle()
                bundle.putString(SearchManager.QUERY, lecture.locationForSearch)
                val destination = SystemActivity(RoomFinderActivity::class.java, bundle)
                NavigationManager.open(context, destination)
            }
        }

        val dayOfWeek = DateTimeFormat.forPattern("EEEE, ").withLocale(Locale.getDefault())
        val timeFormatter = DateTimeFormat.shortTime()

        card_event_action.text = String.format("%s%s - %s", dayOfWeek.print(lecture.start), timeFormatter.print(lecture.start), timeFormatter.print(lecture.end)) // TODO
        card_event_action.setOnClickListener {
            val intent = Intent(context, CalendarActivity::class.java)
            intent.putExtra(Const.EVENT_TIME, lecture.start.getMillis())
            context.startActivity(intent)
        }

    }

    companion object {

        private val IDS = intArrayOf(R.id.lecture_1, R.id.lecture_2, R.id.lecture_3, R.id.lecture_4)

    }

}
