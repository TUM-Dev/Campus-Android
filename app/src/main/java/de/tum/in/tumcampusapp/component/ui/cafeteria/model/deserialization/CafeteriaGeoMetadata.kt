package de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.deserialization

import com.google.gson.annotations.SerializedName

data class CafeteriaGeoMetadata(
        @SerializedName("address")
        var address: String = "",
        @SerializedName("latitude")
        var latitude: Float = 0.0f,
        @SerializedName("longitude")
        var longitude: Float = 0.0f
)