package de.tum.`in`.tumcampusapp.component.tumui.calendar

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.tumui.calendar.model.CalendarItem

class MonthViewEventAdapter(private val events: List<CalendarItem>) : RecyclerView.Adapter<MonthViewEventViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthViewEventViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.calendar_month_event_view, parent, false)
        return MonthViewEventViewHolder(view)
    }

    override fun onBindViewHolder(holder: MonthViewEventViewHolder, position: Int) {
        val event = events[position]
        holder.title.text = event.title
        val background = holder.title.background as GradientDrawable
        background.setColor(event.color!!)
    }

    override fun getItemCount(): Int {
        return events.size
    }
}
