package de.tum.`in`.tumcampusapp.api.navigatum.model.details

import com.google.gson.annotations.SerializedName

data class NavigationMapsDto(
    var default: String = "",
    @SerializedName("roomfinder")
    var roomFinder: RoomFinderMapsDto = RoomFinderMapsDto()
)
