package de.tum.`in`.tumcampusapp.api.navigatum.domain

import de.tum.`in`.tumcampusapp.api.navigatum.model.details.NavigationDetailsDto
import de.tum.`in`.tumcampusapp.api.navigatum.model.details.RoomFinderMapDto

data class NavigationDetails(
    val id: String,
    val name: String,
    val type: String,
    val cordsLat: Float,
    val cordsLon: Float,
    val mapId: String?,
    val mapName: String?,
    val mapImgUrl: String?
) {
    companion object {
        const val BASIC_MAP_URL = "https://nav.tum.sexy/cdn/maps/roomfinder/"
    }

    fun getFullMapImgUrl(): String? {
        if (mapImgUrl == null) {
            return null
        }
        return "$BASIC_MAP_URL$mapImgUrl"
    }
}

fun NavigationDetailsDto.toNavigationDetails(): NavigationDetails {

    val defaultMapId = this.maps.roomFinder.defaultMapId
    val matchingMaps = this.maps.roomFinder.available
        .filter { defaultMapId == it.id }
        .toList()
    var map: RoomFinderMapDto? = null
    if (matchingMaps.isNotEmpty()) {
        map = matchingMaps[0]
    } else if (this.maps.roomFinder.available.isNotEmpty()) {
        map = this.maps.roomFinder.available[0]
    }

    return NavigationDetails(
        id = this.id,
        name = this.name,
        type = this.type,
        cordsLat = this.cords.lat,
        cordsLon = this.cords.lon,
        mapId = map?.id,
        mapName = map?.name,
        mapImgUrl = map?.imgUrl,
    )
}
