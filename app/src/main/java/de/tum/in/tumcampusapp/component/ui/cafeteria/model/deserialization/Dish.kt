package de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.deserialization

import com.google.gson.annotations.SerializedName

data class Dish(
        @SerializedName("name")
        var name: String? = null,
        @SerializedName("ingredients")
        var ingredients: List<String>,
        @SerializedName("dish_type")
        var type: String? = null
)