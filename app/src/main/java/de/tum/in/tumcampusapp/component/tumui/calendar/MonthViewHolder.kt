package de.tum.`in`.tumcampusapp.component.tumui.calendar

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.tum.`in`.tumcampusapp.R

class MonthViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val dayOfMonth: TextView = itemView.findViewById(R.id.cellDayNumber)

    val eventsContainer: RecyclerView = itemView.findViewById(R.id.eventsContainer)
}
