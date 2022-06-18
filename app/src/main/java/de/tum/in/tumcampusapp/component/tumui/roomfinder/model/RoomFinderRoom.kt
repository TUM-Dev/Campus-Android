package de.tum.`in`.tumcampusapp.component.tumui.roomfinder.model

import com.google.gson.Gson
import de.tum.`in`.tumcampusapp.component.other.general.RecentsDao
import de.tum.`in`.tumcampusapp.component.other.general.model.Recent
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

    companion object {
        private const val serialVersionUID = 6631656320611471476L

        @JvmStatic fun toRecent(room: RoomFinderRoom) : Recent {
            val gson = Gson()
            val jsonString = gson.toJson(room)
            return Recent(name = jsonString, type = RecentsDao.ROOMS)
        }

        fun fromRecent(r: Recent): RoomFinderRoom {
            val gson = Gson()
            return gson.fromJson(r.name, RoomFinderRoom::class.java)
        }
    }
}
