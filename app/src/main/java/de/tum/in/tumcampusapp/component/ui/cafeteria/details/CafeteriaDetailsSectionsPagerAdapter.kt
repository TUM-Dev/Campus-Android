package de.tum.`in`.tumcampusapp.component.ui.cafeteria.details

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

/**
 * A [FragmentStatePagerAdapter] that returns a fragment corresponding to one
 * of the sections/tabs/pages.
 */
class CafeteriaDetailsSectionsPagerAdapter(fragmentManager: FragmentManager) : FragmentStatePagerAdapter(fragmentManager) {

    private var cafeteriaId: Int = 0
    private var dates: List<DateTime> = ArrayList()
    private val formatter: DateTimeFormatter = DateTimeFormat.fullDate()

    fun setCafeteriaId(cafeteriaId: Int) {
        this.cafeteriaId = cafeteriaId
    }

    fun update(menuDates: List<DateTime>) {
        dates = menuDates
        notifyDataSetChanged()
    }

    override fun getCount() = dates.size

    override fun getItem(position: Int) =
        CafeteriaDetailsSectionFragment.newInstance(cafeteriaId, dates[position])

    override fun getPageTitle(position: Int) =
        formatter.print(dates[position]).toUpperCase(Locale.getDefault())

    override fun getItemPosition(`object`: Any): Int = PagerAdapter.POSITION_NONE
}
