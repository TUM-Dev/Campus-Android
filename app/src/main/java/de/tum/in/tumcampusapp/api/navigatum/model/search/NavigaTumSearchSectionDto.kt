package de.tum.`in`.tumcampusapp.api.navigatum.model.search

import com.google.gson.annotations.SerializedName
import de.tum.`in`.tumcampusapp.api.navigatum.domain.NavigationEntity

data class NavigaTumSearchSectionDto(
    @SerializedName("facet")
    var type: String = "",
    @SerializedName("entries")
    val entries: List<NavigationEntity> = listOf()
) {
    companion object {
        const val BUILDINGS_TYPE = "sites_buildings"
        const val ROOMS = "rooms"
    }
}
