package de.tum.`in`.tumcampusapp.component.ui.cafeteria.model

import android.content.Context
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.RoomWarnings
import com.google.gson.annotations.SerializedName
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.deserialization.DishPrices
import org.joda.time.DateTime
import java.util.regex.Pattern

/**
 * CafeteriaMenu
 *
 * @param menuId CafeteriaMenu Id (empty for addendum)
 * @param cafeteriaId primary key of the associated cafeteria
 * @param slug cafeteria string identification slug e.g.: "mensa-garching"
 * @param date date on which the dish is served
 * @param dishType
 * @param name
 * @param labels Labels describing the dish. Typically these are allergens.
 * @param calendarWeek calendar week for the current year in which the dish is served
 */
@Entity
@SuppressWarnings(RoomWarnings.DEFAULT_CONSTRUCTOR)
data class CafeteriaMenu(
        @PrimaryKey(autoGenerate = true)
        @SerializedName("menuId")
        var menuId: Int = 0,
        @SerializedName("cafeteriaId")
        var cafeteriaId: Int = 0,
        @SerializedName("cafeteriaSlug")
        var slug: String = "",
        @SerializedName("date")
        var date: DateTime? = null,
        @SerializedName("dishType")
        var dishType: String = "",
        @SerializedName("name")
        var name: String = "",
        @SerializedName("labels")
        var labels: String = "",
        @SerializedName("calendarWeek")
        var calendarWeek: Int = -1,
        @SerializedName("dishPrices")
        var dishPrices: DishPrices
) {

    val tag: String
        get() = "${name}__$cafeteriaId"

    // TODO pricing, dishType enum, label enum?, mensa enum?
    val menuType: MenuType
        get() {
            return when (dishType) {
                "tg" -> MenuType.DAILY_SPECIAL
                "ae" -> MenuType.DISCOUNTED_COURSE
                "akt" -> MenuType.SPECIALS
                "bio" -> MenuType.BIO
                else -> MenuType.SIDE_DISH
            }
        }

    val notificationTitle: String
        get() {
            return REMOVE_DISH_ENUMERATION_PATTERN
                    .matcher(dishType)
                    .replaceAll("")
                    .trim()
        }

    fun getNotificationText(context: Context): String {
        val lines = getNotificationLines(context)
        return if (dishType == MenuType.SPECIALS.toString()) {
            lines.joinToString(", ")
        } else {
            lines.first()
        }
    }

    private fun getNotificationLines(context: Context): List<String> {
        return if (dishType == MenuType.SPECIALS.toString()) {
            // Returns a list of all specials
            name
                    .split("\n")
                    .map { it.trim() }
        } else {
            // Returns a list containing the dish name and the price
            val priceText = getPriceText(context)
            listOfNotNull(name, priceText)
        }
    }

    fun getPriceText(context: Context): String {
        val rolePrice = dishPrices.getRolePrice(context)
        return if (rolePrice.basePrice != 0.0) String.format("%.2f€ + %.2f€", rolePrice.basePrice, rolePrice.pricePerUnit)
        else String.format("%.2f€", rolePrice.pricePerUnit)
    }

    companion object {
        private val REMOVE_DISH_ENUMERATION_PATTERN: Pattern = Pattern.compile("[0-9]")
    }
}
