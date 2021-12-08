package de.tum.`in`.tumcampusapp.component.ui.cafeteria.model

import com.google.gson.annotations.SerializedName
import org.joda.time.DateTime

data class DailyMenu(
        @SerializedName("date")
        var date: DateTime? = null,
        @SerializedName("dishes")
        var dishesForDay: List<Dish>
)