package de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.deserialization

import com.google.gson.annotations.SerializedName

data class CafeteriaMetadata(
        @SerializedName("canteen_id")
        var cafeteriaId: String = "",
        @SerializedName("name")
        var name: String = "",
        @SerializedName("location")
        var geoMetadata: CafeteriaGeoMetadata
)