package de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.deserialization

import com.google.gson.annotations.SerializedName
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.DishPrices

data class Dish(
        @SerializedName("name")
        var name: String,
        @SerializedName("labels")
        var labels: List<String>,
        @SerializedName("dish_type")
        var type: String,
        @SerializedName("prices")
        var prices: DishPrices
)