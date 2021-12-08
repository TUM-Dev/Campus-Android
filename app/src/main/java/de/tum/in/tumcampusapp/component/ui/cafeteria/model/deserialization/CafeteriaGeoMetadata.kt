package de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.deserialization

import com.google.gson.annotations.SerializedName

data class CafeteriaGeoMetadata(
        @SerializedName("address")
        var address: String = "",
        @SerializedName("latitude")
        var latitude: Double = 0.0,
        @SerializedName("longitude")
        var longitude: Double = 0.0
)