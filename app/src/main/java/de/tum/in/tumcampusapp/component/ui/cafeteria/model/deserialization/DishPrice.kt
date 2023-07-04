package de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.deserialization

import com.google.gson.annotations.SerializedName

data class DishPrice(
        @SerializedName("base_price")
        var basePrice: Double,
        @SerializedName("price_per_unit")
        var pricePerUnit: Double,
        @SerializedName("unit")
        var unit: String
)
