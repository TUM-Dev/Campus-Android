package de.tum.`in`.tumcampusapp.component.ui.cafeteria.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.content.Context
import java.util.*
import java.util.regex.Pattern

/**
 * CafeteriaMenu
 *
 * @param id          CafeteriaMenu Id (empty for addendum)
 * @param cafeteriaId Cafeteria ID
 * @param date        Menu date
 * @param typeShort   Short type, e.g. tg
 * @param typeLong    Long type, e.g. Tagesgericht 1
 * @param typeNr      Type ID
 * @param name        Menu name
 */
@Entity
data class CafeteriaMenu(@PrimaryKey(autoGenerate = true)
                         var id: Int = 0,
                         var cafeteriaId: Int = -1,
                         var date: Date? = null,
                         var typeShort: String = "",
                         var typeLong: String = "",
                         var typeNr: Int = -1,
                         var name: String = "") {

    private val formattedName: String
        get() = COMPILE.matcher(name).replaceAll("").trim()

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
            return PATTERN
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
        return if (price != null) "$price €" else null
    }

    companion object {

        private val COMPILE: Pattern = Pattern.compile("\\([^\\)]+\\)")
        private val PATTERN: Pattern = Pattern.compile("[0-9]")

    }

}