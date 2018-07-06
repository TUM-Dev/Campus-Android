package de.tum.`in`.tumcampusapp.component.ui.cafeteria.model

import org.joda.time.DateTime
import org.joda.time.LocalDate
import java.util.*

data class CafeteriaWithMenus(val id: Int) {

    var name: String? = null
    var menus: List<CafeteriaMenu> = ArrayList()
    var menuDates: List<DateTime> = ArrayList()

    val nextMenuDate: DateTime
        get() {
            val now = DateTime.now()
            var nextDate = menuDates
                    .getOrElse(0) {
                        DateTime.now()
                    }

            if (nextDate.isToday() && now.hourOfDay >= 15 && menuDates.size > 1) {
                nextDate = menuDates[1]
            }

            return nextDate
        }
}

fun DateTime.isToday() = LocalDate.now() == LocalDate(this)
