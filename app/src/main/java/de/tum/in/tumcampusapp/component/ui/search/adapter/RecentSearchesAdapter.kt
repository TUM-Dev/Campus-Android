package de.tum.`in`.tumcampusapp.component.ui.search.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.makeramen.roundedimageview.RoundedImageView
import com.squareup.picasso.Picasso
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.general.RecentsDao
import de.tum.`in`.tumcampusapp.component.other.general.model.Recent
import de.tum.`in`.tumcampusapp.component.tumui.lectures.model.Lecture
import de.tum.`in`.tumcampusapp.component.tumui.person.model.Person
import de.tum.`in`.tumcampusapp.component.tumui.roomfinder.model.RoomFinderRoom

class RecentSearchesAdapter(
    private val onSelect: (Recent) -> Unit,
    private val onRemove: (Recent) -> Unit,
) : ListAdapter<Recent, RecentSearchesAdapter.RecentSearchViewHolder>(RecentSearchCallback) {

    class RecentSearchViewHolder(
        itemView: View,
        private val onSelect: (Recent) -> Unit,
        private val onRemove: (Recent) -> Unit,
    ) : RecyclerView.ViewHolder(itemView) {

        private val mainLayout: ConstraintLayout = this.itemView.findViewById(R.id.mainLayout)
        private val clearButton: ImageButton = this.itemView.findViewById(R.id.clearButton)
        private val titleText: TextView = this.itemView.findViewById(R.id.recentSearchTitle)
        private val icon: ImageView = this.itemView.findViewById(R.id.iconImageView)
        private val profilePicture: RoundedImageView = this.itemView.findViewById(R.id.profilePictureImageView)

        private var currentRecentSearch: Recent? = null

        fun bind(recentSearch: Recent) {
            currentRecentSearch = recentSearch
            titleText.text = recentSearch.name

            mainLayout.setOnClickListener { onSelect(recentSearch) }
            clearButton.setOnClickListener { onRemove(recentSearch) }

            when (recentSearch.type) {
                RecentsDao.PERSONS -> {
                    try {
                        val person = Person.fromRecent(recentSearch)
                        titleText.text = person.getFullName()
                        Picasso.get()
                                .load(person.getFullImageUrl())
                                .into(profilePicture)
                    } catch (exception: Exception) {
                        titleText.setText(R.string.not_available_search)
                        profilePicture.setImageDrawable(null)
                    }
                    icon.setImageResource(R.drawable.ic_person)
                    icon.setBackgroundResource(R.drawable.circle_background)
                }
                RecentsDao.ROOMS -> {
                    try {
                        val room = RoomFinderRoom.fromRecent(recentSearch)
                        titleText.text = room.formattedAddress
                    } catch (exception: Exception) {
                        titleText.setText(R.string.not_available_search)
                    }
                    icon.setImageResource(R.drawable.ic_room)
                    icon.setBackgroundResource(R.drawable.search_result_icon_background)
                    profilePicture.setImageDrawable(null)
                }
                RecentsDao.LECTURES -> {
                    try {
                        val lecture = Lecture.fromRecent(recentSearch)
                        titleText.text = lecture.title
                    } catch (exception: Exception) {
                        titleText.setText(R.string.not_available_search)
                    }
                    icon.setImageResource(R.drawable.ic_lecture)
                    icon.setBackgroundResource(R.drawable.search_result_icon_background)
                    profilePicture.setImageDrawable(null)
                }
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentSearchViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.recent_search_row_item, parent, false)
        return RecentSearchViewHolder(view, onSelect, onRemove)
    }

    override fun onBindViewHolder(holder: RecentSearchViewHolder, position: Int) {
        val searchResult = getItem(position)
        holder.bind(searchResult)
    }
}

object RecentSearchCallback : DiffUtil.ItemCallback<Recent>() {
    override fun areItemsTheSame(oldItem: Recent, newItem: Recent): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Recent, newItem: Recent): Boolean {
        return oldItem.name == newItem.name
    }
}