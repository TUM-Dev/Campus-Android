package de.tum.`in`.tumcampusapp.component.tumui.person.model

import com.tickaroo.tikxml.annotation.Element
import com.tickaroo.tikxml.annotation.Xml
import java.io.Serializable

/**
 * Wrapper class holding a list of [Room]s. Note: This model is based on
 * the TUMOnline web service response format for a corresponding request.
 */
@Xml(name = "raeume")
data class RoomList(@Element val rooms: List<Room>? = null) : Serializable {
    companion object {
        private const val serialVersionUID = 1115343203243361774L
    }
}
