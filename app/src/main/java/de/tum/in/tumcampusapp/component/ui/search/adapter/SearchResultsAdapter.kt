package de.tum.`in`.tumcampusapp.component.ui.search.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.makeramen.roundedimageview.RoundedImageView
import com.squareup.picasso.Picasso
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.ui.search.SearchResult

class SearchResultsAdapter(
    private val onClick: (SearchResult) -> Unit
) : ListAdapter<SearchResult, SearchResultsAdapter.SearchResultViewHolder>(SearchResultCallback) {

    class SearchResultViewHolder(
        itemView: View,
        val onClick: (SearchResult) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val searchResultTitleTextView: TextView = this.itemView.findViewById(R.id.titleTextView)
        private val searchResultSubtitleTextView: TextView = this.itemView.findViewById(R.id.subtitleTextView)
        private val searchResultIcon: ImageView = this.itemView.findViewById(R.id.iconImageView)
        private val profilePicture: RoundedImageView = this.itemView.findViewById(R.id.profilePictureImageView)

        private var currentSearchResult: SearchResult? = null

        init {
            itemView.setOnClickListener {
                currentSearchResult?.let {
                    onClick(it)
                }
            }
        }

        fun bind(searchResult: SearchResult) {
            currentSearchResult = searchResult
            searchResultTitleTextView.text = searchResult.title
            searchResultSubtitleTextView.text = searchResult.subtitle

            val imageResource = when (searchResult) {
                is SearchResult.Person -> R.drawable.ic_person
                is SearchResult.Room -> R.drawable.ic_room
                is SearchResult.Lecture -> R.drawable.ic_lecture
            }
            searchResultIcon.setImageResource(imageResource)

            if (searchResult is SearchResult.Person) {
                searchResultSubtitleTextView.visibility = View.GONE
                searchResultIcon.setBackgroundResource(R.drawable.circle_background)
                Picasso.get()
                    .load(searchResult.person.getFullImageUrl())
                    .into(profilePicture)
            } else {

                /**
                 * Adapter does not remove items from the screen, but reuse them for performance
                 * that is why we need to set this values back to original ones
                 * Example: item is displayed as a Person and we hide the subtitle, then user scroll
                 * and the same item becomes now Lecture, and if we do not reset these values in 'else'
                 * then subtitle will be still not visible
                 */

                searchResultSubtitleTextView.visibility = View.VISIBLE
                searchResultIcon.setBackgroundResource(R.drawable.search_result_icon_background)
                profilePicture.setImageDrawable(null)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.search_result_row_item, parent, false)
        return SearchResultViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: SearchResultViewHolder, position: Int) {
        val searchResult = getItem(position)
        holder.bind(searchResult)
    }
}

object SearchResultCallback : DiffUtil.ItemCallback<SearchResult>() {
    override fun areItemsTheSame(oldItem: SearchResult, newItem: SearchResult): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: SearchResult, newItem: SearchResult): Boolean {
        return oldItem.title == newItem.title
    }
}
