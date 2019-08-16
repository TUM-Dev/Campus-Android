package de.tum.`in`.tumcampusapp.component.ui.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.ui.search.SearchResult.Lecture
import de.tum.`in`.tumcampusapp.component.ui.search.SearchResult.Person
import de.tum.`in`.tumcampusapp.component.ui.search.SearchResult.Room
import kotlinx.android.synthetic.main.list_item_search_result.view.iconImageView
import kotlinx.android.synthetic.main.list_item_search_result.view.subtitleTextView
import kotlinx.android.synthetic.main.list_item_search_result.view.titleTextView

class SearchResultsAdapter(
    private val onItemClick: (SearchResult) -> Unit
) : ListAdapter<SearchResult, SearchResultsAdapter.ViewHolder>(
    object : DiffUtil.ItemCallback<SearchResult>() {
        override fun areItemsTheSame(
            oldItem: SearchResult,
            newItem: SearchResult
        ): Boolean = when {
            oldItem is Lecture && newItem is Lecture -> oldItem.lecture.lectureId == newItem.lecture.lectureId
            oldItem is Person && newItem is Person -> oldItem.person.id == newItem.person.id
            oldItem is Room && newItem is Room -> oldItem.room.room_id == newItem.room.room_id
            else -> false
        }

        override fun areContentsTheSame(
            oldItem: SearchResult,
            newItem: SearchResult
        ): Boolean = when {
            oldItem is Lecture && newItem is Lecture -> oldItem.lecture == newItem.lecture
            oldItem is Person && newItem is Person -> oldItem.person == newItem.person
            oldItem is Room && newItem is Room -> oldItem.room == newItem.room
            else -> false
        }
    }
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_search_result, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onItemClick)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(
            searchResult: SearchResult,
            onItemClick: (SearchResult) -> Unit
        ) = with(itemView) {
            titleTextView.text = searchResult.title
            subtitleTextView.isVisible = searchResult.subtitle.isNullOrBlank().not()
            subtitleTextView.text = searchResult.subtitle

            val imageResource = when (searchResult) {
                is Lecture -> R.drawable.ic_outline_school_24px
                is Person -> R.drawable.ic_person
                is Room -> R.drawable.ic_room
            }
            iconImageView.setImageResource(imageResource)

            setOnClickListener { onItemClick(searchResult) }
        }

    }

}