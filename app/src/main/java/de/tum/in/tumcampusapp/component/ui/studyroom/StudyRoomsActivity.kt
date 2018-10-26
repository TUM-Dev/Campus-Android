package de.tum.`in`.tumcampusapp.component.ui.studyroom

import android.R.id.text1
import android.R.layout.simple_spinner_dropdown_item
import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.generic.activity.ActivityForAccessingTumCabe
import de.tum.`in`.tumcampusapp.component.tumui.roomfinder.RoomFinderActivity
import de.tum.`in`.tumcampusapp.component.ui.studyroom.model.StudyRoomGroup
import kotlinx.android.synthetic.main.activity_study_rooms.*

/**
 * Shows information about reservable study rooms.
 */
class StudyRoomsActivity : ActivityForAccessingTumCabe<List<StudyRoomGroup>>(
        R.layout.activity_study_rooms
), AdapterView.OnItemSelectedListener {
    private var groups: List<StudyRoomGroup> = emptyList()
    private var groupId = -1

    private val viewPager: ViewPager by lazy { findViewById<ViewPager>(R.id.pager) }
    private val sectionsPagerAdapter by lazy { StudyRoomsPagerAdapter(supportFragmentManager) }
    private val studyRoomGroupManager by lazy { StudyRoomGroupManager(this) }

    // Drop-down navigation
    private val studyRoomGroupsSpinner: Spinner
        get() {
            val groupAdapter = object : ArrayAdapter<StudyRoomGroup>
            (this, simple_spinner_dropdown_item, text1, groups) {
                val inflater = LayoutInflater.from(context)

                override fun getDropDownView(pos: Int, ignored: View?, parent: ViewGroup): View {
                    val v = inflater.inflate(simple_spinner_dropdown_item, parent, false)
                    val studyRoomGroup = getItem(pos) ?: return v
                    val nameTextView = v.findViewById<TextView>(text1)
                    nameTextView.text = studyRoomGroup.name
                    return v
                }
            }

            groupAdapter.setDropDownViewResource(simple_spinner_dropdown_item)
            return findViewById<Spinner>(R.id.spinner).apply {
                adapter = groupAdapter
                onItemSelectedListener = this@StudyRoomsActivity
            }
        }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadStudyRooms()
    }

    override fun onRefresh() = loadStudyRooms()

    /**
     * A new study room group has been selected -> Switch.
     */
    override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
        groupId = groups[pos].id
        changeViewPagerAdapter(groupId)
    }

    override fun onNothingSelected(adapterView: AdapterView<*>) = Unit

    /**
     * StudyRoomGroupDetailsFragment sets the buttons tag to the StudyRoom.code
     * This code can be used to find the room via the RoomFinder
     */
    fun openLink(view: View) {
        val link = view.tag as String
        val roomCode = link.substringAfter(' ') // ???

        with(Intent()) {
            putExtra(SearchManager.QUERY, roomCode)
            setClass(this@StudyRoomsActivity, RoomFinderActivity::class.java)
            startActivity(this)
        }
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

    /**
     * Change the group ID of the view pager, carefully unsetting the adapter while updating
     */
    private fun changeViewPagerAdapter(selectedRoomGroupId: Int) {
        viewPager.adapter = null
        sectionsPagerAdapter.setStudyRoomGroupId(selectedRoomGroupId)
        viewPager.adapter = sectionsPagerAdapter
    }

    private fun loadStudyRooms() {
        fetch(apiClient.studyRoomGroups)
    }

    override fun onDownloadSuccessful(body: List<StudyRoomGroup>) {
        studyRoomGroupManager.updateDatabase(body) {
            runOnUiThread {
                groups = body
                displayStudyRooms()
            }
        }
    }

    private fun displayStudyRooms() {
        selectCurrentSpinnerItem()
        spinnerContainer.visibility = View.VISIBLE
        showLoadingEnded()
    }
}
