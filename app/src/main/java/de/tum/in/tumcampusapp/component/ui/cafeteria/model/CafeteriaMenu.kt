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

    val notificationTitle: String
        get() {
            return PATTERN
                    .matcher(typeLong)
                    .replaceAll("")
                    .trim()
        }

    fun getNotificationText(context: Context): String {
        val priceText = getPriceText(context)
        val formattedPriceText = COMPILE
                .matcher(priceText)
                .replaceAll("")
                .trim()

        return name + formattedPriceText
    }

    private fun getPriceText(context: Context): String {
        val rolePrices = CafeteriaPrices.getRolePrices(context)
        val price = rolePrices[typeLong]
        return if (price != null) "\n$price €" else ""
    }

    companion object {

        val COMPILE = Pattern.compile("\\([^\\)]+\\)")
        val PATTERN = Pattern.compile("[0-9]")

    }

}