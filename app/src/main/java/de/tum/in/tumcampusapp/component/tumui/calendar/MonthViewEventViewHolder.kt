package de.tum.`in`.tumcampusapp.component.tumui.calendar

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.tum.`in`.tumcampusapp.R

class MonthViewEventViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val title: TextView = view.findViewById(R.id.eventTitle)
}