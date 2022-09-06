package de.tum.`in`.tumcampusapp.api.navigatum.model.details

import com.google.gson.annotations.SerializedName

data class RoomFinderMapsDto(
    var available: List<RoomFinderMapDto> = listOf(),
    @SerializedName("default")
    var defaultMapId: String = ""
)
