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
            var nextDate = menuDates
                    .getOrElse(0) { DateUtils.getDateTimeString(Date()) }
            val nextDateDate = DateUtils.getDate(nextDate)

            // TODO: Improve this mess and comment

            if (AndroidDateUtils.isToday(nextDateDate.time)
                    && now.get(Calendar.HOUR_OF_DAY) >= 15 && menuDates.size > 1) {
                nextDate = menuDates[1]
            }

            return nextDate
        }

}