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

class NextLectureCardViewHolder(
        itemView: View,
        interactionListener: CardInteractionListener
) : CardViewHolder(itemView, interactionListener) {

    private var isExpanded = false
    private var didBind = false

    private val divider = itemView.findViewById<View>(R.id.divider)
    private val moreTextView = itemView.findViewById<TextView>(R.id.moreTextView)
    private val additionalLecturesLayout = itemView.findViewById<LinearLayout>(R.id.additionalLecturesLayout)
    private val lectureContainerLayout = itemView.findViewById<LinearLayout>(R.id.currentLecturesContainer)

    fun bind(items: List<NextLectureCard.CardCalendarItem>) = with(itemView) {
        // Split lectures in progress / upcoming lectures
        var (currentLectures, remaining) = items.partition {
            it.start.isBeforeNow
        }
        // if no lecture is currently in progress, show the next upcoming lectures (that start within 15 minutes of each other)
        // TODO: if a lecture starts right after the last currentLecture (or is same day?), we should also directly display it
        if (currentLectures.isEmpty()) {
            val nextLectureDate = remaining.first().start.plusMinutes(15)

            remaining.partition {
                it.start.isBefore(nextLectureDate)
            }.let { (before, after) ->
                currentLectures = before
                remaining = after
            }
        }

        showLectures(currentLectures)

        if (remaining.isEmpty()) {
            divider.visibility = View.GONE
            moreTextView.visibility = View.GONE
            return
        }

        if (didBind.not()) {
            // This is the first call of bind. Therefore, we inflate any additional NextLectureViews
            // for upcoming events.
            remaining.forEach { item ->
                val lectureView = NextLectureView(context).apply {
                    setLecture(item)
                }
                additionalLecturesLayout.addView(lectureView)
            }
            toggleMoreButton(remaining.size)
            didBind = didBind.not()
        }

        moreTextView.setOnClickListener {
            isExpanded = isExpanded.not()
            additionalLecturesLayout.visibility = if (isExpanded) View.VISIBLE else View.GONE
            toggleMoreButton(remaining.size)
        }
    }

    private fun toggleMoreButton(remainingItems: Int) = with(itemView) {
        val moreTextFormatString = if (isExpanded) R.string.next_lecture_hide else R.string.next_lecture_show_more
        moreTextView.text = context.getString(moreTextFormatString, remainingItems)

        val icon = if (isExpanded) R.drawable.ic_arrow_up_blue else R.drawable.ic_arrow_down_blue
        moreTextView.addCompoundDrawablesWithIntrinsicBounds(start = icon)

        // If possible, run the transition on the parent RecyclerView to incorporate sibling views
        TransitionManager.beginDelayedTransition((this.parent ?: this) as ViewGroup)
    }

    private fun showLectures(lectures: List<NextLectureCard.CardCalendarItem>) = with(itemView) {
        // If we don't remove all views, we will add lectures again on refresh
        lectureContainerLayout.removeAllViews()

        lectures.forEach { item ->
            val lectureView = NextLectureView(context).apply {
                setLecture(item)
            }
            lectureContainerLayout.addView(lectureView)
        }
    }
}
