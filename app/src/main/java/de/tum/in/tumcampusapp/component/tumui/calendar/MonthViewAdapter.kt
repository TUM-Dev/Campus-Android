package de.tum.`in`.tumcampusapp.component.tumui.calendar

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.tumui.calendar.model.CalendarItem

class MonthViewAdapter(private var daysOfMonth: ArrayList<String>, private var eventMap: Map<String, List<CalendarItem>>)
    : RecyclerView.Adapter<MonthViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.calendar_month_view_cell, parent, false)
        val layoutParams = view.layoutParams
        layoutParams.height = (parent.height * 1/6)
        return MonthViewHolder(view)
    }

    override fun onBindViewHolder(holder: MonthViewHolder, position: Int) {
        val events = eventMap[daysOfMonth[position]]

        holder.dayOfMonth.text = daysOfMonth[position]
        holder.eventsContainer.adapter = events?.let { MonthViewEventAdapter(it) }
        holder.eventsContainer.layoutManager = LinearLayoutManager(holder.itemView.context)
    }

    override fun getItemCount(): Int {
        return daysOfMonth.size
    }

    fun updateData(daysOfMonth: ArrayList<String>, eventMap: Map<String, List<CalendarItem>>) {
        this.daysOfMonth = daysOfMonth
        this.eventMap = eventMap
        notifyDataSetChanged()
    }

}
