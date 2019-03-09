package de.tum.`in`.tumcampusapp.component.ui.studyroom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.generic.adapter.GridEqualSpacingDecoration
import de.tum.`in`.tumcampusapp.utils.Const

/**
 * Fragment for each study room group. Shows study room details in a list.
 */
class StudyRoomGroupDetailsFragment : Fragment() {
    private var groupId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.run {
            if (containsKey(Const.STUDY_ROOM_GROUP_ID)) {
                groupId = getInt(Const.STUDY_ROOM_GROUP_ID)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val rootView = inflater.inflate(R.layout.fragment_study_rooms, container, false)
        val studyRooms = StudyRoomGroupManager(requireContext()).getAllStudyRoomsForGroup(groupId)

        if (studyRooms.isEmpty()) {
            rootView.findViewById<View>(R.id.study_room_placeholder).visibility = View.VISIBLE
            return rootView
        }

        rootView.findViewById<RecyclerView>(R.id.fragment_item_detail_recyclerview).apply {
            val spanCount = 2
            layoutManager = GridLayoutManager(context, spanCount)
            adapter = StudyRoomAdapter(this@StudyRoomGroupDetailsFragment, studyRooms)

            val spacing = Math.round(resources.getDimension(R.dimen.material_card_view_padding))
            addItemDecoration(GridEqualSpacingDecoration(spacing, spanCount))
        }
        return rootView
    }
}
