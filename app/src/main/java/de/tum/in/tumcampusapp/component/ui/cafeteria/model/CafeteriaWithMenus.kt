package de.tum.`in`.tumcampusapp.component.ui.cafeteria.model

import android.content.Context
import android.content.Intent
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.activity.CafeteriaActivity
import de.tum.`in`.tumcampusapp.utils.Const
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

    fun getIntent(context: Context): Intent? =
            Intent(context, CafeteriaActivity::class.java).apply {
                putExtra(Const.CAFETERIA_ID, id)
            }

}

fun Date.isToday() = AndroidDateUtils.isToday(this.time)

val Calendar.hour: Int
    get() = this.get(Calendar.HOUR_OF_DAY)