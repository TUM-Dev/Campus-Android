package de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.deserialization

import com.google.gson.annotations.SerializedName

data class CafeteriaResponse(
    @SerializedName("canteens")
    var cafeterias: List<CafeteriaData>
)