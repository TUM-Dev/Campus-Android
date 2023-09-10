package de.tum.`in`.tumcampusapp.component.tumui.roomfinder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.alamkanak.weekview.WeekView
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.tumui.calendar.WidgetCalendarItem
import de.tum.`in`.tumcampusapp.utils.Const

@Deprecated(
"""should be removed
 Logic from WeekViewFragment and RoomFinderSchedule should be reimplemented
 in new NavigationDetailsFragment, instead of legacy RoomFinderDetailsActivity.
 More info: https://github.com/TUM-Dev/Campus-Android/pull/1462"
"""
)
class WeekViewFragment : Fragment() {

    private var roomApiCode: String? = null
    private lateinit var weekView: WeekView<WidgetCalendarItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        roomApiCode = arguments?.getString(Const.ROOM_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_day_view, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        weekView = view.findViewById(R.id.weekView)
        weekView.goToHour(8)
    }

    companion object {

        fun newInstance(roomApiCode: String): WeekViewFragment {
            val fragment = WeekViewFragment()
            fragment.arguments = Bundle().apply { putString(Const.ROOM_ID, roomApiCode) }
            return fragment
        }
    }
}
