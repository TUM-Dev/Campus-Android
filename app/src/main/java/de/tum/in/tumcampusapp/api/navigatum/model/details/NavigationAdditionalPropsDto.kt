package de.tum.`in`.tumcampusapp.api.navigatum.model.details

import com.google.gson.annotations.SerializedName

data class NavigationAdditionalPropsDto(
    @SerializedName("computed")
    var propsList: List<NavigationPropertyDto> = listOf()
)
