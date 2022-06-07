package de.tum.`in`.tumcampusapp.component.ui.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import de.tum.`in`.tumcampusapp.R

class SearchResultsAdapter(
        private val onClick: (SearchResult) -> Unit,
) : ListAdapter<SearchResult, SearchResultsAdapter.SearchResultViewHolder>(SearchResultCallback) {

    class SearchResultViewHolder(
        itemView: View,
        val onClick: (SearchResult) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val searchResultTitleTextView: TextView = this.itemView.findViewById(R.id.title)
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