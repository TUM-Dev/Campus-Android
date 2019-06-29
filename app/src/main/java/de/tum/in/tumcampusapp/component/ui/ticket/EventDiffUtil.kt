package de.tum.`in`.tumcampusapp.component.ui.ticket

import androidx.recyclerview.widget.DiffUtil
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.EventItem

class EventDiffUtil(
        private val oldItems: List<EventItem>,
        private val newItems: List<EventItem>
) : DiffUtil.Callback() {

    override fun getOldListSize() = oldItems.size
    override fun getNewListSize() = newItems.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
            oldItems[oldItemPosition].getIdForComparison() == newItems[newItemPosition].getIdForComparison()

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
            oldItems[oldItemPosition] == newItems[newItemPosition]
}
