package de.tum.`in`.tumcampusapp.models.tumo

import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

import java.io.Serializable

/**
 * Wrapper class holding a list of [Room]s. Note: This model is based on
 * the TUMOnline web service response format for a corresponding request.
 */
@Root(name = "raeume")
data class RoomList(@field:ElementList(inline = true, required = false)
                    var rooms: List<Room> = emptyList()) : Serializable {
    companion object {
        private const val serialVersionUID = 1115343203243361774L
    }
}
