package de.tum.`in`.tumcampusapp.component.tumui.roomfinder.model

/**
 * This class is used as a model for maps in Roomfinder retrofit request.
 */
@Deprecated("""Please use NavigationDetailsDto instead""")
data class RoomFinderMap(
    var map_id: String = "",
    var description: String = ""
)
