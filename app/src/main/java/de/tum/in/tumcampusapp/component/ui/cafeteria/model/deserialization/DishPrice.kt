package de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.deserialization

import com.google.gson.annotations.SerializedName
import java.util.Locale

data class DishPrice(
        @SerializedName("base_price")
        var basePrice: Double,
        @SerializedName("price_per_unit")
        var pricePerUnit: Double,
        @SerializedName("unit")
        var unit: String
) {
    val basePriceString: String
        get() {
            return prettifyDoublePrice(basePrice)
        }

    val pricePerUnitString: String
        get() {
            return prettifyDoublePrice(pricePerUnit)
        }

    private fun prettifyDoublePrice(price: Double): String {
        return if (price.rem(1).equals(0.0)) String.format(Locale.GERMAN, "%.0f€", price)
        else String.format(Locale.GERMAN, "%.2f€", price)
    }
}
