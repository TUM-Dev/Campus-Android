package de.tum.`in`.tumcampusapp.component.ui.cafeteria

import android.app.TimePickerDialog
import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.utils.Utils
import kotlinx.android.synthetic.main.notification_schedule_listitem.view.*
import org.joda.time.LocalTime
import org.joda.time.format.DateTimeFormat
import java.util.*

class CafeteriaNotificationSettingsAdapter(
        private val context: Context,
        private val dailySchedule: List<CafeteriaNotificationTime>
) : RecyclerView.Adapter<CafeteriaNotificationSettingsAdapter.ViewHolder>(), OnNotificationTimeChangedListener {

    private val settings = CafeteriaNotificationSettings.getInstance(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.notification_schedule_listitem, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notificationTime = dailySchedule[position]
        holder.bind(notificationTime, settings, this)
    }

    override fun onTimeChanged(position: Int, newTime: LocalTime) {
        if (newTime.hourOfDay !in MIN_HOUR..MAX_HOUR) {
            val text = context.getString(
                    R.string.invalid_notification_time_format_string, MIN_HOUR, MAX_HOUR)
            Utils.showToast(context, text)
            return
        }

        updateNotificationTime(position, newTime)
    }

    override fun onCheckChanged(position: Int, isChecked: Boolean) {
        if (!isChecked) {
            updateNotificationTime(position, null)
            return
        }

        // If the user re-enables a notification on a particular day, we retrieve the last known
        // time or the default time
        val time = dailySchedule[position]
        val newTime = settings.retrieveLocalTimeOrDefault(time.weekday)
        updateNotificationTime(position, newTime)
    }

    private fun updateNotificationTime(position: Int, newTime: LocalTime?) {
        dailySchedule[position].apply {
            time = newTime
        }
        notifyItemChanged(position)
    }

    override fun getItemCount() = dailySchedule.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val dayFormatter = DateTimeFormat.forPattern("EEEE").withLocale(Locale.getDefault())
        private val timeFormatter = DateTimeFormat.shortTime().withLocale(Locale.getDefault())

        fun bind(time: CafeteriaNotificationTime,
                 settings: CafeteriaNotificationSettings,
                 listener: OnNotificationTimeChangedListener) = with(itemView) {
            val dayOfWeekString = dayFormatter.print(time.weekday)
            weekdayTextView.text = dayOfWeekString

            notificationActiveCheckBox.setOnCheckedChangeListener(null)
            notificationActiveCheckBox.isChecked = time.time != null
            time.time?.let {
                notificationTimeTextView.text = timeFormatter.print(it)
            }

            notificationTimeTextView.setOnClickListener {
                val defaultTime = settings.retrieveLocalTimeOrDefault(time.weekday)
                val timePicker = TimePickerDialog(context, { _, hour, minute ->
                    val newTime = LocalTime.now()
                            .withHourOfDay(hour)
                            .withMinuteOfHour(minute)
                    listener.onTimeChanged(adapterPosition, newTime)
                }, defaultTime.hourOfDay, defaultTime.minuteOfHour, true)
                timePicker.show()
            }

            notificationActiveCheckBox.setOnCheckedChangeListener { _, isChecked ->
                listener.onCheckChanged(adapterPosition, isChecked)
            }
        }

    }

    companion object {
        private const val MIN_HOUR = 6
        private const val MAX_HOUR = 14
    }

}
