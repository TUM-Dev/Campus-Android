package de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.deserialization

import android.content.Context
import com.google.gson.annotations.SerializedName
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Utils

data class DishPrices(
        @SerializedName("students")
        var studentPrice: DishPrice,
        @SerializedName("staff")
        var staffPrice: DishPrice,
        @SerializedName("guests")
        var guestPrice: DishPrice) {

    fun getRolePrice(context: Context): DishPrice {
        // TODO check from context if possible, return only student price for now
        return this.studentPrice
    }


    /**
     * Gets a [Map] which hashes cafeteria menu's long title
     * to prices.
     *
     * @param context Context
     * @return hash map
     */
    // TODO check usages and remove
    companion object {

        fun getRolePrices(context: Context): Map<String, String> {
            return when (Utils.getSetting(context, Const.ROLE, "")) {
                "0" -> mapOf<String, String>("a" to "b")
                "1" -> mapOf<String, String>("a" to "b")
                "2" -> mapOf<String, String>("a" to "b")
                else -> mapOf<String, String>("a" to "b")
            }
        }

        fun getPrice(context: Context, menuType: String): String? = getRolePrices(context)[menuType]

    }
}
