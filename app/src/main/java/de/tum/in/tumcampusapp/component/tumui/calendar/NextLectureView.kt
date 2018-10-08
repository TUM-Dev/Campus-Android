package de.tum.`in`.tumcampusapp.component.tumui.calendar

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.navigation.NavigationManager
import de.tum.`in`.tumcampusapp.component.other.navigation.SystemActivity
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.DateTimeUtils
import kotlinx.android.synthetic.main.layout_card_lecture.view.*


class NextLectureView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val view = LayoutInflater.from(context).inflate(R.layout.layout_card_lecture, this, true)

    fun setLecture(lecture: NextLectureCard.CardCalendarItem) {
        view.lectureTitleTextView.text = lecture.title
        view.lectureTimeTextView.text = DateTimeUtils.formatFutureTime(lecture.start, context)

        if (lecture.location == null || lecture.location.isEmpty()) {
            lectureLocationTextView.visibility = View.GONE
        } else {
            lectureLocationTextView.text = lecture.location
        }

        view.setOnClickListener {
            val bundle = Bundle().apply {
                putLong(Const.EVENT_TIME, lecture.start.millis)
                putString(Const.KEY_EVENT_ID, lecture.id)
            }
            val destination = SystemActivity(CalendarActivity::class.java, bundle)
            NavigationManager.open(context, destination)
        }
    }

}
