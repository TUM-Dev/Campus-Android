package de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.deserialization

import com.google.gson.annotations.SerializedName

data class CafeteriaResponse(
    @SerializedName("number")
    var calendarWeek: Int,
    @SerializedName("year")
    var year: Int,
    @SerializedName("days")
    var dishesForWeek: List<DailyMenu>
)