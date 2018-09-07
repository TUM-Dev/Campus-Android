package de.tum.`in`.tumcampusapp.component.ui.ticket

import android.support.v7.util.DiffUtil
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.Event

class EventDiffUtil(
        private val oldItems: List<Event>,
        private val newItems: List<Event>
) : DiffUtil.Callback() {

    override fun getOldListSize() = oldItems.size

    override fun getNewListSize() = newItems.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldItems[oldItemPosition].id == newItems[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldItems[oldItemPosition] == newItems[newItemPosition]
    }

}
