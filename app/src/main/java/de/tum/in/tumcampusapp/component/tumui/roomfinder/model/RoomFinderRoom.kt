package de.tum.`in`.tumcampusapp.component.tumui.roomfinder.model

import de.tum.`in`.tumcampusapp.component.other.generic.adapter.SimpleStickyListHeadersAdapter
import java.io.Serializable

/**
 * This class is used as a model for rooms in Roomfinder retrofit request.
 * @param name This is the campus name
 */
data class RoomFinderRoom(
    var campus: String = "",
    var address: String = "",
    var info: String = "",
    var arch_id: String = "",
    var room_id: String = "",
    var room_code: String = "",
    private val name: String = ""
) : SimpleStickyListHeadersAdapter.SimpleStickyListItem, Serializable {

    private val formattedName: String
        get() {
            return if (name == "null")
                ""
            else
                name
        }

    val formattedAddress: String
        get() = address.trim()
            .replace("(", " (")
            .replace("\\s+".toRegex(), " ")

    override fun getHeadName() = formattedName

    override fun getHeaderId() = getHeadName()
}
