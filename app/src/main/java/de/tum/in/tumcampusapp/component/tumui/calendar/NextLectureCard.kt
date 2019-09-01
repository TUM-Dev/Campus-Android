package de.tum.`in`.tumcampusapp.component.tumui.calendar

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.tumui.calendar.model.CalendarItem
import de.tum.`in`.tumcampusapp.component.ui.overview.CardInteractionListener
import de.tum.`in`.tumcampusapp.component.ui.overview.CardManager
import de.tum.`in`.tumcampusapp.component.ui.overview.card.Card
import de.tum.`in`.tumcampusapp.component.ui.overview.card.CardViewHolder
import org.joda.time.DateTime
import java.util.*

class NextLectureCard(context: Context) : Card(CardManager.CARD_NEXT_LECTURE, context, "card_next_lecture") {

    private val calendarController: CalendarController = CalendarController(context)

    private val lectures = ArrayList<CardCalendarItem>()

    override val optionsMenuResId: Int
        get() = R.menu.card_popup_menu

    override fun updateViewHolder(viewHolder: RecyclerView.ViewHolder) {
        if (viewHolder is NextLectureCardViewHolder) {
            viewHolder.bind(lectures)
        }
    }

    override fun discard(editor: SharedPreferences.Editor) {
        val item = lectures.lastOrNull() ?: return
        editor.putLong(NEXT_LECTURE_DATE, item.start.millis)
    }

    override fun shouldShow(prefs: SharedPreferences): Boolean {
        val item = lectures.firstOrNull() ?: return false
        val prevTime = prefs.getLong(NEXT_LECTURE_DATE, 0)
        return item.start.millis > prevTime
    }

    override fun getId(): Int {
        return 0
    }

    fun setLectures(calendarItems: List<CalendarItem>) {
        calendarItems.mapTo(lectures) { calendarItem ->
            CardCalendarItem(
                    id = calendarItem.nr,
                    start = calendarItem.dtstart,
                    end = calendarItem.dtend,
                    title = calendarItem.getFormattedTitle(),
                    locations = calendarController.getLocationsForEvent(calendarItem.nr)
            )
        }
    }

    data class CardCalendarItem(
        val id: String,
        val title: String,
        val start: DateTime,
        val end: DateTime,
        val locations: List<String>?
    ) {
        val locationString: String
            get() {
                val locationString = StringBuilder()
                for (location in locations.orEmpty()) {
                    locationString.append(location)
                    locationString.append("\n")
                }
                // Remove the last new line character.
                locationString.deleteCharAt(locationString.length - 1)
                return locationString.toString()
            }
    }

    companion object {
        private const val NEXT_LECTURE_DATE = "next_date"
        @JvmStatic
        fun inflateViewHolder(parent: ViewGroup, interactionListener: CardInteractionListener): CardViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.card_next_lecture_item, parent, false)
            return NextLectureCardViewHolder(view, interactionListener)
        }
    }
}
