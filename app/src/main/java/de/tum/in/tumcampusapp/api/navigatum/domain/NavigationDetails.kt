package de.tum.`in`.tumcampusapp.api.navigatum.domain

import de.tum.`in`.tumcampusapp.api.navigatum.model.details.NavigationDetailsDto
import de.tum.`in`.tumcampusapp.component.other.locations.model.Geo

data class NavigationDetails(
    val id: String,
    val name: String,
    val type: String,
    val typeCommonName: String,
    val geo: Geo,
    val parentIds: List<String> = listOf(),
    val parentsNames: List<String> = listOf(),
    val properties: List<NavigationProperty> = listOf(),
    val availableMaps: List<RoomfinderMap> = listOf()
) {
    fun getFormattedParentNames(): String {
        return parentsNames.reduce { acc, parentName -> "$acc \\ $parentName" }
    }

    fun getParentId(): String? {
        return parentIds.lastOrNull()
    }
}

fun NavigationDetailsDto.toNavigationDetails(): NavigationDetails {

    return NavigationDetails(
        id = this.id,
        name = this.name,
        type = this.type,
        typeCommonName = this.typeCommonName,
        geo = Geo(this.cords.lat, this.cords.lon),
        parentIds = this.parentIds.drop(1),
        parentsNames = this.parentNames,
        properties = this.additionalProperties.propsList
            .map { it.toNavigationProperty() },
        availableMaps = this.maps.roomFinder.available
            .map { it.toNavigationMap() }
    )
}
