package de.tum.`in`.tumcampusapp.component.ui.studyroom

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter

import de.tum.`in`.tumcampusapp.utils.Const

class StudyRoomsPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

    private var currentId: Int = 0

    /**
     * Create a StudyRoomGroupDetailsFragment with the correct parameter for the selected group
     */
    override fun getItem(position: Int): Fragment {
        return StudyRoomGroupDetailsFragment().apply {
            arguments = Bundle().apply {
                putInt(Const.STUDY_ROOM_GROUP_ID, currentId)
            }
        }
    }

    override fun getCount() = 1

    internal fun setStudyRoomGroupId(selectedGroupId: Int) {
        currentId = selectedGroupId
    }
}
