package de.tum.`in`.tumcampusapp.component.tumui.calendar

import android.os.Bundle
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.navigation.NavigationManager
import de.tum.`in`.tumcampusapp.component.other.navigation.SystemActivity
import de.tum.`in`.tumcampusapp.component.ui.overview.card.CardViewHolder
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.DateTimeUtils
import kotlinx.android.synthetic.main.card_next_lecture_item.view.*
import kotlinx.android.synthetic.main.layout_card_lecture.view.*

class NextLectureCardViewHolder(itemView: View) : CardViewHolder(itemView) {

    private var isExpanded = false

    fun bind(items: List<NextLectureCard.CardCalendarItem>) = with(itemView) {
        showLecture(items.first())

        if (items.size == 1) {
            moreTextView.visibility = View.GONE
            return
        }

        val remaining = items.drop(1)
        remaining.forEach { item ->
            val lectureView = NextLectureView(context).apply {
                setLecture(item)
            }
            additionalLecturesLayout.addView(lectureView)
        }

        toggleMoreButton(remaining.size)

        moreTextView.setOnClickListener {
            isExpanded = isExpanded.not()
            additionalLecturesLayout.visibility = if (isExpanded) View.VISIBLE else View.GONE
            toggleMoreButton(remaining.size)
        }
    }

    private fun toggleMoreButton(remainingItems: Int) = with(itemView) {
        val textFormatString = if (isExpanded) R.string.next_lecture_hide_more else R.string.next_lecture_show_more
        moreTextView.text = context.getString(textFormatString, remainingItems)
        TransitionManager.beginDelayedTransition(this as ViewGroup)
    }

    private fun showLecture(lecture: NextLectureCard.CardCalendarItem) = with(itemView) {
        lectureTitleTextView.text = lecture.title
        lectureTimeTextView.text = DateTimeUtils.formatFutureTime(lecture.start, context)

        if (lecture.location == null || lecture.location.isEmpty()) {
            lectureLocationTextView.visibility = View.GONE
        } else {
            lectureLocationTextView.text = lecture.location
        }

        setOnClickListener {
            val bundle = Bundle().apply { putLong(Const.EVENT_TIME, lecture.start.millis) }
            val destination = SystemActivity(CalendarActivity::class.java, bundle)
            NavigationManager.open(context, destination)
        }
    }

}
