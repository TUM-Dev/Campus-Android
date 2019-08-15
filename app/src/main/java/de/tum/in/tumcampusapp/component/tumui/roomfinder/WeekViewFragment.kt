package de.tum.`in`.tumcampusapp.component.tumui.roomfinder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.alamkanak.weekview.MonthChangeListener
import com.alamkanak.weekview.WeekView
import com.alamkanak.weekview.WeekViewDisplayable
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.component.tumui.calendar.WidgetCalendarItem
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Utils
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.util.*

class WeekViewFragment : Fragment(), MonthChangeListener<WidgetCalendarItem> {

    private val eventsCache = HashMap<Calendar, List<WeekViewDisplayable<WidgetCalendarItem>>>()

    private var roomApiCode: String? = null
    private lateinit var weekView: WeekView<WidgetCalendarItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        roomApiCode = arguments?.getString(Const.ROOM_ID)
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_day_view, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        weekView = view.findViewById(R.id.weekView)
        weekView.setMonthChangeListener(this)
        weekView.goToHour(8)
    }

    override fun onMonthChange(startDate: Calendar,
                               endDate: Calendar): List<WeekViewDisplayable<WidgetCalendarItem>> {
        if (!isLoaded(startDate)) {
            loadEventsInBackground(startDate, endDate)
            return emptyList()
        }

        return eventsCache[startDate] ?: throw IllegalStateException()
    }

    private fun isLoaded(start: Calendar): Boolean {
        return eventsCache[start] != null
    }

    private fun loadEventsInBackground(start: Calendar, end: Calendar) {
        // Populate the week view with the events of the month to display
        Thread {
            val format = DateTimeFormat.forPattern("yyyyMMdd").withLocale(Locale.getDefault())

            val formattedStartTime = format.print(DateTime(start))
            val formattedEndTime = format.print(DateTime(end))

            // Convert to the proper type
            val events = fetchEventList(roomApiCode, formattedStartTime, formattedEndTime)

            requireActivity().runOnUiThread {
                eventsCache[start] = events
                weekView.notifyDataSetChanged()
            }
        }.start()
    }

    private fun fetchEventList(roomId: String?, startDate: String, endDate: String): List<WeekViewDisplayable<WidgetCalendarItem>> {
        try {
            val schedules = TUMCabeClient
                    .getInstance(requireActivity())
                    .fetchSchedule(roomId, startDate, endDate) ?: return emptyList()

            // Convert to the proper type
            val events = ArrayList<WeekViewDisplayable<WidgetCalendarItem>>()
            for (schedule in schedules) {
                val calendarItem = WidgetCalendarItem.create(schedule)
                calendarItem.color = ContextCompat.getColor(requireContext(), R.color.event_lecture)
                events.add(calendarItem)
            }

            return events
        } catch (e: Exception) {
            Utils.log(e)
        }

        return emptyList()
    }

    companion object {

        fun newInstance(roomApiCode: String): WeekViewFragment {
            val fragment = WeekViewFragment()
            fragment.arguments = Bundle().apply { putString(Const.ROOM_ID, roomApiCode) }
            return fragment
        }
    }

}