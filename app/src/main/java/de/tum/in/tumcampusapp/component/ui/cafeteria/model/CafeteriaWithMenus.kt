package de.tum.`in`.tumcampusapp.component.ui.cafeteria.model

import de.tum.`in`.tumcampusapp.utils.DateUtils
import java.util.*

typealias AndroidDateUtils = android.text.format.DateUtils

data class CafeteriaWithMenus(val id: Int) {

    var name: String? = null
    var menus: List<CafeteriaMenu> = ArrayList()
    var menuDates: List<String> = ArrayList()

    val nextMenuDate: Date
        get() {
            val now = Calendar.getInstance();
            var nextDate = menuDates
                    .map { s -> DateUtils.getDate(s) }
                    .getOrElse(0) {
                        Date()
                    }

            if (nextDate.isToday() && now.hour >= 15 && menuDates.size > 1) {
                nextDate = DateUtils.getDate(menuDates[1])
            }

            return nextDate
        }

    val nextMenuDateText: String
        get() = DateUtils.getDateTimeString(nextMenuDate)

}

fun Date.isToday() = AndroidDateUtils.isToday(this.time)

val Calendar.hour: Int
    get() = this.get(Calendar.HOUR_OF_DAY)