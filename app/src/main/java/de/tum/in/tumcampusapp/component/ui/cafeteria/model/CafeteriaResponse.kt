package de.tum.`in`.tumcampusapp.component.ui.cafeteria.model

import com.google.gson.annotations.SerializedName

data class CafeteriaResponse(
    @SerializedName("canteens")
    var cafeterias: List<CafeteriaData>
)