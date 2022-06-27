package de.tum.`in`.tumcampusapp.api.navigatum.model.search

import com.google.gson.annotations.SerializedName

data class NavigaTumSearchResponseDto(
    @SerializedName("sections")
    val sections: List<NavigaTumSearchSectionDto> = listOf(),
)
