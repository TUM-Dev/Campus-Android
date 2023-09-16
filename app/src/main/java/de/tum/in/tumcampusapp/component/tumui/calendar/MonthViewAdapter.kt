package de.tum.`in`.tumcampusapp.component.tumui.calendar

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.tumui.calendar.model.CalendarItem
import org.joda.time.LocalDate
import java.time.YearMonth

class MonthViewAdapter(
    private var daysOfMonth: ArrayList<String>,
    private var eventMap: Map<Int, List<CalendarItem>>,
    private var selectedDate: LocalDate
) :
        RecyclerView.Adapter<MonthViewHolder>() {

    private lateinit var monthViewEventAdapter: MonthViewEventAdapter
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.calendar_month_view_cell, parent, false)
        val layoutParams = view.layoutParams
        layoutParams.height = (parent.height * 1 / 6)
        monthViewEventAdapter = MonthViewEventAdapter(emptyList(), selectedDate)
        return MonthViewHolder(view)
    }

    override fun onBindViewHolder(holder: MonthViewHolder, position: Int) {
        val events = eventMap[position]

        holder.dayOfMonth.text = daysOfMonth[position]

        if (events.isNullOrEmpty()) {
            holder.eventsContainer.adapter = null
        } else {
            holder.eventsContainer.adapter = monthViewEventAdapter
            holder.eventsContainer.layoutManager = LinearLayoutManager(holder.itemView.context)
            monthViewEventAdapter.updateData(events, selectedDate)
        }

        if (position < selectedDate.withDayOfMonth(1).dayOfWeek - 1 ||
            position > selectedDate.withDayOfMonth(1).dayOfWeek - 1 +
            YearMonth.of(selectedDate.year, selectedDate.monthOfYear).lengthOfMonth() - 1) {
            holder.dayOfMonth.alpha = 0.2F
        } else {
            holder.dayOfMonth.alpha = 1F
        }
    }

    override fun getItemCount(): Int {
        return daysOfMonth.size
    }

    fun updateData(daysOfMonth: ArrayList<String>, eventMap: Map<Int, List<CalendarItem>>, selectedDate: LocalDate) {
        this.daysOfMonth = daysOfMonth
        this.eventMap = eventMap
        this.selectedDate = selectedDate
        notifyDataSetChanged()
    }
}
