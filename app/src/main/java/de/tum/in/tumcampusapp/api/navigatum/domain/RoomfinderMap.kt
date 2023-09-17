package de.tum.`in`.tumcampusapp.api.navigatum.domain

import de.tum.`in`.tumcampusapp.api.navigatum.model.details.RoomFinderMapDto

data class RoomfinderMap(
    val mapId: String,
    val mapName: String,
    val mapImgUrl: String,
    val pointerXCord: Int,
    val pointerYCord: Int,
    val mapImgWidth: Int,
    val mapImgHeight: Int
)

fun RoomFinderMapDto.toNavigationMap(): RoomfinderMap {
    val basicMapUrl = "https://nav.tum.de/cdn/maps/roomfinder/"
    return RoomfinderMap(
        mapId = this.id,
        mapName = this.name,
        mapImgUrl = "$basicMapUrl${this.imgUrl}",
        pointerXCord = this.x,
        pointerYCord = this.y,
        mapImgWidth = this.width,
        mapImgHeight = this.height
    )
}
