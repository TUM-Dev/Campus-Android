package de.tum.`in`.tumcampusapp.component.tumui.calendar

import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.ui.overview.CardInteractionListener
import de.tum.`in`.tumcampusapp.component.ui.overview.card.CardViewHolder
import de.tum.`in`.tumcampusapp.utils.addCompoundDrawablesWithIntrinsicBounds

class NextLectureCardViewHolder(itemView: View, interactionListener: CardInteractionListener) : CardViewHolder(itemView, interactionListener) {

    private var isExpanded = false

    private val divider = itemView.findViewById<View>(R.id.divider)
    private val moreTextView = itemView.findViewById<TextView>(R.id.moreTextView)
    private val additionalLecturesLayout = itemView.findViewById<LinearLayout>(R.id.additionalLecturesLayout)
    private val lectureContainerLayout = itemView.findViewById<LinearLayout>(R.id.currentLecturesContainer)

    fun bind(items: List<NextLectureCard.CardCalendarItem>) = with(itemView) {
        isExpanded = false

        // Split events into "day of next lecture" and "not same day", the latter will be hidden behind the expand button
        val firstLectureDate = items.first().start.toLocalDate()
        val (currentEvents, futureEvents) = items.partition {
            it.start.toLocalDate().equals(firstLectureDate)
        }

        addLectures(lectureContainerLayout, currentEvents)

        if (futureEvents.isEmpty()) {
            divider.visibility = View.GONE
            moreTextView.visibility = View.GONE
            return
        }

        addLectures(additionalLecturesLayout, futureEvents)

        updateExpansion(futureEvents.size)

        moreTextView.setOnClickListener {
            isExpanded = isExpanded.not()
            toggleMoreButton(futureEvents.size)
        }
    }

    private fun updateExpansion(remainingItems: Int) = with(itemView) {
        val moreTextFormatString = if (isExpanded) R.string.next_lecture_hide else R.string.next_lecture_show_more
        moreTextView.text = context.getString(moreTextFormatString, remainingItems)

        val icon = if (isExpanded) R.drawable.ic_arrow_up_blue else R.drawable.ic_arrow_down_blue
        moreTextView.addCompoundDrawablesWithIntrinsicBounds(start = icon)

        additionalLecturesLayout.visibility = if (isExpanded) View.VISIBLE else View.GONE
    }

    private fun toggleMoreButton(remainingItems: Int) = with(itemView) {
        updateExpansion(remainingItems)

        // If possible, run the transition on the parent RecyclerView to incorporate sibling views
        TransitionManager.beginDelayedTransition((this.parent ?: this) as ViewGroup)
    }

    private fun addLectures(layout: LinearLayout, lectures: List<NextLectureCard.CardCalendarItem>) = with(itemView) {
        layout.removeAllViews()

        lectures.forEach { item ->
            val lectureView = NextLectureView(context).apply {
                setLecture(item)
            }
            layout.addView(lectureView)
        }
    }
}
