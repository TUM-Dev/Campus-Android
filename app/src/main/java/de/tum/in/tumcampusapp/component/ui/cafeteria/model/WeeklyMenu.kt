package de.tum.`in`.tumcampusapp.component.ui.cafeteria.model

import com.google.gson.annotations.SerializedName

data class WeeklyMenu(
        @SerializedName("number")
        var weekOfYear: Short = -1,
        @SerializedName("year")
        var year: Short = -1,
        @SerializedName("days")
        var dishesForWeek: List<DailyMenu>
)