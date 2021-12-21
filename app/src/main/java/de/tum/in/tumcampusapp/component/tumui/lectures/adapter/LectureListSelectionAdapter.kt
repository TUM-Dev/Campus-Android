package de.tum.`in`.tumcampusapp.component.tumui.lectures.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.CompoundButton

import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.tumui.calendar.CalendarController
import de.tum.`in`.tumcampusapp.component.tumui.calendar.model.CalendarItem
import de.tum.`in`.tumcampusapp.utils.Utils
import java.lang.Long.parseLong

class LectureListSelectionAdapter(
    context: Context,
    private val calendarItems: List<CalendarItem>,
    private val appWidgetId: Int
) : BaseAdapter(), CompoundButton.OnCheckedChangeListener {

    private val calendarController: CalendarController = CalendarController(context)
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        // save new preferences
        Utils.logVerbose("Widget asked to change ${buttonView.text} to $isChecked")
        if (isChecked) {
            calendarController.deleteLectureFromBlacklist(this.appWidgetId, buttonView.text as String)
        } else {
            calendarController.addLectureToBlacklist(this.appWidgetId, buttonView.text as String)
        }
    }

    override fun getCount() = calendarItems.size

    override fun getItem(position: Int) = calendarItems[position]

    override fun getItemId(position: Int) = parseLong(calendarItems[position].nr)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View = convertView ?: inflater.inflate(R.layout.list_timetable_configure_item, parent, false)

        val checkBox = view.findViewById<CheckBox>(R.id.timetable_configure_item)
        checkBox.isChecked = !calendarItems[position].blacklisted
        checkBox.text = calendarItems[position].title
        checkBox.setOnCheckedChangeListener(this)

        return view
    }
}
