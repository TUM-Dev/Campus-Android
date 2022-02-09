package de.tum.`in`.tumcampusapp.component.ui.cafeteria.model

import android.content.Context
import android.content.Intent
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.activity.CafeteriaActivity
import de.tum.`in`.tumcampusapp.utils.Const
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.Period
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

    fun getIntent(context: Context): Intent? =
            Intent(context, CafeteriaActivity::class.java).apply {
                putExtra(Const.CAFETERIA_ID, id)
            }

    // We notify the user when the cafeteria typically opens
    val notificationTime: DateTime
        get() = nextMenuDate
                .withHourOfDay(11)
                .withMinuteOfHour(0)
                .withSecondOfMinute(0)

    // Cafeteria is typically opened from 11 to 14 ~= 3 hours
    val notificationDuration: Long
        get() = Period.hours(3).millis.toLong()
}

fun DateTime.isToday() = LocalDate.now() == LocalDate(this)
