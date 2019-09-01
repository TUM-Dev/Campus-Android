package de.tum.`in`.tumcampusapp.component.other.generic.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter

import de.tum.`in`.tumcampusapp.R
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter

/**
 * This adapter class produces just one single item saying now search results
 */
class NoResultsAdapter(context: Context) :
        ArrayAdapter<String>(context, R.layout.listview_simple_item_center, arrayOf(context.getString(R.string.no_search_result))), StickyListHeadersAdapter {

    // Generate header view
    override fun getHeaderView(pos: Int, convertView: View, parent: ViewGroup) = View(context)

    override fun getHeaderId(i: Int): Long = 0

    override fun isEnabled(position: Int) = false
}
