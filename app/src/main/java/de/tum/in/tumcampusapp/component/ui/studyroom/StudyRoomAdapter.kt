package de.tum.`in`.tumcampusapp.component.ui.studyroom

import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.ui.studyroom.model.StudyRoom
import de.tum.`in`.tumcampusapp.utils.Utils
import org.joda.time.format.DateTimeFormat
import java.util.*

class StudyRoomAdapter(private val fragment: Fragment, private val studyRooms: List<StudyRoom>) :
        RecyclerView.Adapter<StudyRoomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudyRoomViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.two_line_list_item, parent, false)
        return StudyRoomViewHolder(view)
    }

    override fun onBindViewHolder(holder: StudyRoomViewHolder, position: Int) {
        val (_, code, name, buildingName, _, occupiedUntil) = studyRooms[position]

        holder.apply {
            openRoomFinderButton.setText(R.string.go_to_room)
            openRoomFinderButton.tag = code
            headerTextView.text = "$code - $name"
            val detailsText = StringBuilder(buildingName)
            val isOccupied = occupiedUntil != null && !occupiedUntil.isBeforeNow
            if (isOccupied) {
                detailsText.append("<br>${fragment.getString(R.string.occupied)} <b>")
                        .append(DateTimeFormat.forPattern("HH:mm")
                                .withLocale(Locale.getDefault())
                                .print(occupiedUntil))
                        .append("</b>")
            }

            detailsTextView.text = Utils.fromHtml(detailsText.toString())

            val colorResId = if (isOccupied) R.color.study_room_occupied else R.color.study_room_free
            val color = ContextCompat.getColor(holder.itemView.context, colorResId)
            cardView.setCardBackgroundColor(color)
        }
    }

    override fun getItemCount() = studyRooms.size
}