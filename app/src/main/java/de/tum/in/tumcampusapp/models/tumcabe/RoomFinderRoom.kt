package de.tum.`in`.tumcampusapp.models.tumcabe

import de.tum.`in`.tumcampusapp.adapters.SimpleStickyListHeadersAdapter.SimpleStickyListItem
import java.io.Serializable

/**
 * This class is used as a model for rooms in Roomfinder retrofit request.
 * @param name This is the campus name
 */
data class RoomFinderRoom(val campus: String, val address: String, val info: String, val arch_id: String, val room_id: String,
                          private val name: String?) : SimpleStickyListItem, Serializable {

    fun getName(): String {
        return if (name == null || name == "null")
            ""
        else
            name
    }

    override fun getHeadName() = getName()

    override fun getHeaderId() = headName

    companion object {
        private const val serialVersionUID = 6631656320611471476L
    }
}
