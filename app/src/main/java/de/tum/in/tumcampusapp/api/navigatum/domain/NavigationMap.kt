package de.tum.`in`.tumcampusapp.api.navigatum.domain

import de.tum.`in`.tumcampusapp.api.navigatum.model.details.RoomFinderMapDto

data class NavigationMap(
    val mapId: String,
    val mapName: String,
    val mapImgUrl: String,
    val pointerXCord: Int,
    val pointerYCord: Int,
    val mapImgWidth: Int,
    val mapImgHeight: Int
)

fun RoomFinderMapDto.toNavigationMap(): NavigationMap {
    val BASIC_MAP_URL = "https://nav.tum.sexy/cdn/maps/roomfinder/"
    return NavigationMap(
        mapId = this.id,
        mapName = this.name,
        mapImgUrl = "$BASIC_MAP_URL${this.imgUrl}",
        pointerXCord = this.x,
        pointerYCord = this.y,
        mapImgWidth = this.width,
        mapImgHeight = this.height
    )
}
