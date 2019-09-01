package de.tum.`in`.tumcampusapp.component.ui.cafeteria.model

import android.content.Context
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.RoomWarnings
import com.google.gson.annotations.SerializedName
import org.joda.time.DateTime
import java.util.regex.Pattern

/**
 * CafeteriaMenu
 *
 * @param id CafeteriaMenu Id (empty for addendum)
 * @param cafeteriaId Cafeteria ID
 * @param date Menu date
 * @param typeShort Short type, e.g. tg
 * @param typeLong Long type, e.g. Tagesgericht 1
 * @param typeNr Type ID
 * @param name Menu name
 */
@Entity
@SuppressWarnings(RoomWarnings.DEFAULT_CONSTRUCTOR)
data class CafeteriaMenu(
    @PrimaryKey(autoGenerate = true)
    @SerializedName("id")
    var id: Int = 0,
    @SerializedName("mensa_id")
    var cafeteriaId: Int = -1,
    @SerializedName("date")
    var date: DateTime? = null,
    @SerializedName("type_short")
    var typeShort: String = "",
    @SerializedName("type_long")
    var typeLong: String = "",
        // If a menu does not have a type number, it is a
        // side dish and is assigned type number 10
    @SerializedName("type_nr")
    var typeNr: Int = 10,
    @SerializedName("name")
    var name: String = ""
) {

    private val formattedName: String
        get() = REMOVE_PARENTHESES_PATTERN.matcher(name).replaceAll("").trim()

    val tag: String
        get() = "${name}__$cafeteriaId"

    val menuType: MenuType
        get() {
            return when (typeShort) {
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
                    .matcher(typeLong)
                    .replaceAll("")
                    .trim()
        }

    fun getNotificationText(context: Context): String {
        val lines = getNotificationLines(context)
        return if (menuType == MenuType.SPECIALS) {
            lines.joinToString(", ")
        } else {
            lines.first()
        }
    }

    fun getNotificationLines(context: Context): List<String> {
        return if (menuType == MenuType.SPECIALS) {
            // Returns a list of all specials
            formattedName
                    .split("\n")
                    .map { it.trim() }
        } else {
            // Returns a list containing the dish name and the price
            val priceText = getPriceText(context)
            listOfNotNull(formattedName, priceText)
        }
    }

    private fun getPriceText(context: Context): String? {
        val rolePrices = CafeteriaPrices.getRolePrices(context)
        val price = rolePrices[typeLong]
        return price?.run { "$this €" }
    }

    companion object {

        private val REMOVE_PARENTHESES_PATTERN: Pattern = Pattern.compile("\\([^\\)]+\\)")
        private val REMOVE_DISH_ENUMERATION_PATTERN: Pattern = Pattern.compile("[0-9]")
    }
}
