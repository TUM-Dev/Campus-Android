package de.tum.`in`.tumcampusapp.component.ui.studyroom

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.generic.fragment.FragmentForAccessingTumCabe
import de.tum.`in`.tumcampusapp.component.ui.studyroom.model.StudyRoomGroup
import de.tum.`in`.tumcampusapp.databinding.FragmentStudyRoomsBinding
import org.jetbrains.anko.support.v4.runOnUiThread

class StudyRoomsFragment : FragmentForAccessingTumCabe<List<StudyRoomGroup>>(
    R.layout.fragment_study_rooms,
    R.string.study_rooms
), AdapterView.OnItemSelectedListener {

    private val sectionsPagerAdapter by lazy { StudyRoomsPagerAdapter(childFragmentManager) }
    private val studyRoomGroupManager by lazy { StudyRoomGroupManager(requireContext()) }

    private var groups = emptyList<StudyRoomGroup>()
    private var groupId: Int = -1

    private val binding by viewBinding(FragmentStudyRoomsBinding::bind)

    // Drop-down navigation
    private val studyRoomGroupsSpinner: Spinner
        get() {
            val groupAdapter = object : ArrayAdapter<StudyRoomGroup>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                android.R.id.text1,
                groups
            ) {
                val inflater = LayoutInflater.from(context)

                override fun getDropDownView(pos: Int, ignored: View?, parent: ViewGroup): View {
                    val v = inflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false)
                    val studyRoomGroup = getItem(pos) ?: return v
                    val nameTextView = v.findViewById<TextView>(android.R.id.text1)
                    nameTextView.text = studyRoomGroup.name
                    return v
                }
            }

            groupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            return binding.spinner.apply {
                adapter = groupAdapter
                onItemSelectedListener = this@StudyRoomsFragment
            }
        }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        groupId = groups[position].id
        changeViewPagerAdapter(groupId)
    }

    override fun onNothingSelected(adapterView: AdapterView<*>?) = Unit

    override fun onStart() {
        super.onStart()
        loadStudyRooms()
    }

    override fun onRefresh() {
        loadStudyRooms()
    }

    /**
     * Change the group ID of the view pager, carefully unsetting the adapter while updating
     */
    private fun changeViewPagerAdapter(selectedRoomGroupId: Int) {
        with(binding) {
            pager.adapter = null
            sectionsPagerAdapter.setStudyRoomGroupId(selectedRoomGroupId)
            pager.adapter = sectionsPagerAdapter
        }

    }

    private fun loadStudyRooms() {
        fetch(apiClient.studyRoomGroups)
    }

    override fun onDownloadSuccessful(response: List<StudyRoomGroup>) {
        studyRoomGroupManager.updateDatabase(response) {
            runOnUiThread {
                groups = response
                displayStudyRooms()
            }
        }
    }

    private fun displayStudyRooms() {
        selectCurrentSpinnerItem()
        binding.spinnerContainer.visibility = View.VISIBLE
        showLoadingEnded()
    }

    private fun selectCurrentSpinnerItem() {
        groups.forEachIndexed { i, (id) ->
            if (groupId == -1 || groupId == id) {
                groupId = id
                studyRoomGroupsSpinner.setSelection(i)
                return
            }
        }
    }

    companion object {
        fun newInstance() = StudyRoomsFragment()
    }
}
