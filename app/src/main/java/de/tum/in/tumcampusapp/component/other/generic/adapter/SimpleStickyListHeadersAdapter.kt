package de.tum.`in`.tumcampusapp.component.other.generic.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import de.tum.`in`.tumcampusapp.R
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter

/**
 * An abstract StickyListHeadersAdapter helps to reduce redundant work for using StickyListHeaders.
 * It implements some method required by StickyListHeadersAdapter, including getHeaderView getHeaderId,
 * getCount, getItem and getItemId.
 * By extending this class only getView needs to implemented.
 * On the other hand this class requires the data model implementing its interface to get header name and id.
 *
 * @param <T> the data model
</T> */
abstract class SimpleStickyListHeadersAdapter<T : SimpleStickyListHeadersAdapter.SimpleStickyListItem>(
        var context: Context,
        var itemList: MutableList<T>
) : BaseAdapter(), StickyListHeadersAdapter {

    private val filters: MutableList<String>
    val inflater: LayoutInflater = LayoutInflater.from(context)

    init {
        filters = itemList.map { it.getHeaderId() }.distinct().toMutableList()
    }

    // needs to be implemented by subclass
    abstract override fun getView(position: Int, convertView: View?, parent: ViewGroup): View

    override fun getHeaderView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: HeaderViewHolder
        val view: View

        if (convertView == null) {
            holder = HeaderViewHolder()
            view = inflater.inflate(R.layout.header, parent, false)
            holder.text = view.findViewById(R.id.lecture_header)
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as HeaderViewHolder
        }

        val headerText = generateHeaderName(itemList[position])
        holder.text?.text = headerText

        return view
    }

    /**
     * Generate header for this item.
     * This method can be overridden if the header name needs to be modified.
     *
     * @param item the item
     * @return the header for this item
     */
    open fun generateHeaderName(item: T): String = item.getHeadName()

    override fun getHeaderId(i: Int): Long = filters.indexOf(itemList[i].getHeaderId()).toLong()

    override fun getCount(): Int = itemList.size

    override fun getItem(position: Int) = itemList[position]

    override fun getItemId(position: Int): Long = position.toLong()

    // Header view
    private class HeaderViewHolder {
        internal var text: TextView? = null
    }

    interface SimpleStickyListItem {
        fun getHeadName(): String
        fun getHeaderId(): String
    }
}