package de.tum.`in`.tumcampusapp.component.tumui.lectures.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.tumui.lectures.activity.LecturesAppointmentsActivity
import de.tum.`in`.tumcampusapp.component.tumui.lectures.model.LectureAppointment
import de.tum.`in`.tumcampusapp.utils.DateTimeUtils
import de.tum.`in`.tumcampusapp.utils.Utils
import org.joda.time.format.DateTimeFormat

/**
 * Generates the output of the ListView on the [LecturesAppointmentsActivity] activity.
 */
class LectureAppointmentsListAdapter(context: Context, // list of Appointments to one lecture
                                     private val appointmentList: List<LectureAppointment>) : BaseAdapter() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    // date formats for the day output
    private val endHoursOutput = DateTimeFormat.mediumTime()
    private val startDateOutput = DateTimeFormat.mediumDateTime()
    private val endDateOutput = DateTimeFormat.mediumDateTime()

    override fun getCount() = appointmentList.size

    override fun getItem(position: Int) = appointmentList[position]

    override fun getItemId(position: Int) = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: ViewHolder
        val view: View

        if (convertView == null) {
            view = inflater.inflate(R.layout.activity_lecturesappointments_listview, parent, false)

            // save UI elements in view holder
            holder = ViewHolder()
            holder.appointmentTime = view.findViewById(R.id.tvTerminZeit)
            holder.appointmentLocation = view.findViewById(R.id.tvTerminOrt)
            holder.appointmentDetails = view.findViewById(R.id.tvTerminBetreff)
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as ViewHolder
        }

        val lvItem = appointmentList[position]
        holder.appointmentLocation?.text = lvItem.location
        holder.appointmentDetails?.text = getAppointmentDetails(lvItem)
        holder.appointmentTime?.text = Utils.fromHtml(getAppointmentTime(lvItem))

        return view
    }

    private fun getAppointmentTime(lvItem: LectureAppointment): String {
        val start = lvItem.startTime
        val end = lvItem.endTime

        // output if same day: we only show the date once
        val output = StringBuilder()
        if (DateTimeUtils.isSameDay(start, end)) {
            output.append(startDateOutput.print(start))
                    .append("–")
                    .append(endHoursOutput.print(end))
        } else {
            // show it normally
            output.append(startDateOutput.print(start))
                    .append("–")
                    .append(endDateOutput.print(end))
        }

        // grey it, if in past
        if (start.isBeforeNow) {
            output.insert(0, "<font color=\"#444444\">")
            output.append("</font>")
        }

        return output.toString()
    }

    private fun getAppointmentDetails(lvItem: LectureAppointment): String {
        val details = StringBuilder(lvItem.type ?: "")
        val title = lvItem.title
        if (title != null && title.isNotEmpty()) {
            details.append(" - ").append(lvItem.title)
        }
        return details.toString()
    }

    // the layout
    internal class ViewHolder {
        var appointmentDetails: TextView? = null
        var appointmentLocation: TextView? = null
        var appointmentTime: TextView? = null
    }
}
