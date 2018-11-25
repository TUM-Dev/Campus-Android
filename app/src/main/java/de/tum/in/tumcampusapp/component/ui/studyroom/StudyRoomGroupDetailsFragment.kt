package de.tum.`in`.tumcampusapp.component.ui.studyroom

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.generic.adapter.GridEqualSpacingDecoration
import de.tum.`in`.tumcampusapp.component.ui.studyroom.di.StudyRoomsModule
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.injector
import javax.inject.Inject

/**
 * Fragment for each study room group. Shows study room details in a list.
 */
class StudyRoomGroupDetailsFragment : Fragment() {

    @Inject
    lateinit var localRepository: StudyRoomGroupLocalRepository

    private val groupId: Int by lazy {
        arguments?.getInt(Const.STUDY_ROOM_GROUP_ID) ?: throw IllegalStateException()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        injector.studyRoomsComponent()
                .studyRoomsModule(StudyRoomsModule())
                .build()
                .inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val rootView = inflater.inflate(R.layout.fragment_item_detail, container, false)
        val studyRooms = localRepository.getAllStudyRoomsForGroup(groupId)

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
