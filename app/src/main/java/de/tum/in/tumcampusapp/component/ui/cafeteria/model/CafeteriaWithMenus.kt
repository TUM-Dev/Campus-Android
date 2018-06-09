package de.tum.`in`.tumcampusapp.component.ui.cafeteria.model

import de.tum.`in`.tumcampusapp.utils.DateUtils
import java.util.*

typealias AndroidDateUtils = android.text.format.DateUtils

data class CafeteriaWithMenus(val id: Int) {

    var name: String? = null
    var menus: List<CafeteriaMenu> = ArrayList()
    var menuDates: List<String> = ArrayList()

    val nextMenuDate: String
        get() {
            val now = Calendar.getInstance();
            var nextDateString = menuDates
                    .getOrElse(0) {
                        DateUtils.getDateTimeString(Date())
                    }
            val nextDate = DateUtils.getDate(nextDateString)

            if (nextDate.isToday() && now.hour >= 15 && menuDates.size > 1) {
                nextDateString = menuDates[1]
            }

            return nextDateString
        }

}

fun Date.isToday() = AndroidDateUtils.isToday(this.time)

val Calendar.hour: Int
    get() = this.get(Calendar.HOUR_OF_DAY)