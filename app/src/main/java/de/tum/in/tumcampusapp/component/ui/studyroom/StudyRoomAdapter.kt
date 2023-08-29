package de.tum.`in`.tumcampusapp.component.ui.studyroom

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.commit
import androidx.recyclerview.widget.RecyclerView
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.ui.search.SearchFragment
import de.tum.`in`.tumcampusapp.component.ui.studyroom.model.StudyRoom
import de.tum.`in`.tumcampusapp.utils.Utils
import org.joda.time.format.DateTimeFormat
import java.util.Locale

class StudyRoomAdapter(private val fragment: Fragment, private val studyRooms: List<StudyRoom>) :
    RecyclerView.Adapter<StudyRoomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudyRoomViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_studyroom_detail, parent, false)
        return StudyRoomViewHolder(view)
    }

    override fun onBindViewHolder(holder: StudyRoomViewHolder, position: Int) {
        val (_, code, name, buildingName, _, occupiedUntil) = studyRooms[position]

        holder.apply {
            openRoomFinderButton.setText(R.string.go_to_room)
            openRoomFinderButton.tag = code
            headerTextView.text = code
            val isOccupied = occupiedUntil != null && !occupiedUntil.isBeforeNow

            val detailsText = StringBuilder("$name<br>$buildingName")
            if (isOccupied) {
                val time = DateTimeFormat.forPattern("HH:mm")
                    .withLocale(Locale.getDefault())
                    .print(occupiedUntil)
                detailsText.append("<br>${fragment.getString(R.string.occupied)} <b>$time</b>")
            }

            detailsTextView.text = Utils.fromHtml(detailsText.toString())

            val colorResId = if (isOccupied) R.color.study_room_occupied else R.color.study_room_free
            val color = ContextCompat.getColor(holder.itemView.context, colorResId)
            cardView.setCardBackgroundColor(color)

            /* Overwrite click listener from xml and open roomfinder */
            openRoomFinderButton.setOnClickListener {
                val link = it.tag as String
                val roomCode = link.substringAfter(' ') // ???

                val activity = it.context as FragmentActivity
                activity.supportFragmentManager.commit {
                    replace(R.id.contentFrame, SearchFragment.newInstance(roomCode))
                }
            }
        }
    }

    override fun getItemCount() = studyRooms.size
}
