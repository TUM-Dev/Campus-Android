package de.tum.`in`.tumcampusapp.api.navigatum.domain

import de.tum.`in`.tumcampusapp.api.navigatum.model.details.NavigationDetailsDto

data class NavigationDetails(
    val id: String,
    val name: String,
    val type: String,
    val cordsLat: Double,
    val cordsLon: Double,
    val parentsList: List<String> = listOf(),
    val properties: List<NavigationProperty> = listOf(),
    val availableMaps: List<NavigationMap> = listOf()
) {
    fun getFormattedParentNames(): String {
        return parentsList.reduce { acc, parentName -> "$acc \\ $parentName" }
    }
}

fun NavigationDetailsDto.toNavigationDetails(): NavigationDetails {

    return NavigationDetails(
        id = this.id,
        name = this.name,
        type = this.type,
        cordsLat = this.cords.lat,
        cordsLon = this.cords.lon,
        parentsList = this.parentNames,
        properties = this.additionalProperties.propsList
            .map { it.toNavigationProperty() },
        availableMaps = this.maps.roomFinder.available
            .map { it.toNavigationMap() }
    )
}
