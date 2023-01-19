package de.tum.`in`.tumcampusapp.api.navigatum.model.details

import com.google.gson.annotations.SerializedName

data class NavigationDetailsDto(
    val id: String = "",
    val name: String = "",
    @SerializedName("parents")
    val parentIds: List<String> = listOf(),
    @SerializedName("parent_names")
    val parentNames: List<String> = listOf(),
    val type: String = "",
    @SerializedName("type_common_name")
    val typeCommonName: String = "",
    @SerializedName("props")
    val additionalProperties: NavigationAdditionalPropsDto = NavigationAdditionalPropsDto(),
    @SerializedName("coords")
    val cords: NavigationCordsDto = NavigationCordsDto(),
    val maps: NavigationMapsDto = NavigationMapsDto()
)
