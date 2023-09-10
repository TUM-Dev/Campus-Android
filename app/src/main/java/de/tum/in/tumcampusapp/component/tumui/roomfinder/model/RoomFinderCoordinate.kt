package de.tum.`in`.tumcampusapp.component.tumui.roomfinder.model

@Deprecated("""Please use NavigationDetailsDto instead""")
data class RoomFinderCoordinate(
    var utm_zone: String = "",
    var utm_easting: String = "",
    var utm_northing: String = "",
    var error: String = ""
)
