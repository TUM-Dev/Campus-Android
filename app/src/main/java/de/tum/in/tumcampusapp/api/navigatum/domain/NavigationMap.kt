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
) {
    companion object {
        const val BASIC_MAP_URL = "https://nav.tum.sexy/cdn/maps/roomfinder/"
    }

    fun getFullMapImgUrl(): String {
        return "$BASIC_MAP_URL$mapImgUrl"
    }
}

fun RoomFinderMapDto.toNavigationMap(): NavigationMap {
    return NavigationMap(
        mapId = this.id,
        mapName = this.name,
        mapImgUrl = this.imgUrl,
        pointerXCord = this.x,
        pointerYCord = this.y,
        mapImgWidth = this.width,
        mapImgHeight = this.height
    )
}
