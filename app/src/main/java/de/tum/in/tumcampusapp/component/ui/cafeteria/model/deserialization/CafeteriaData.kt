package de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.deserialization

import com.google.gson.annotations.SerializedName

data class CafeteriaData(
        @SerializedName("version")
        var version: String,
        @SerializedName("canteen_id")
        var cafeteriaId: String,
        @SerializedName("weeks")
        var menusByWeeks: List<WeeklyMenu>
)