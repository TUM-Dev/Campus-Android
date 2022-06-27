package de.tum.`in`.tumcampusapp.api.navigatum.model.details

import com.google.gson.annotations.SerializedName

data class RoomFinderMapDto(
    var id: String = "",
    var name: String = "",
    @SerializedName("file")
    var imgUrl: String = "",
    var height: Int = 0,
    var width: Int = 0,
    var x: Int = 0,
    var y: Int = 0,
    var scale: String = ""
)
