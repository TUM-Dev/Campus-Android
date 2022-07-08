package de.tum.`in`.tumcampusapp.api.navigatum.model.details

import com.google.gson.annotations.SerializedName

data class NavigationDetailsDto(
    var id: String = "",
    var name: String = "",
    @SerializedName("parent_names")
    var parentNames: List<String> = listOf(),
    var type: String = "",
    @SerializedName("type_common_name")
    var typeCommonName: String = "",
    @SerializedName("coords")
    val cords: NavigationCordsDto = NavigationCordsDto(),
    var maps: NavigationMapsDto = NavigationMapsDto()
)
