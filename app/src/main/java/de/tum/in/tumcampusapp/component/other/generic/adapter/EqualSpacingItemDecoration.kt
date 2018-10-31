package de.tum.`in`.tumcampusapp.component.other.generic.adapter

import android.graphics.Rect
import androidx.recyclerview.widget.RecyclerView
import android.view.View

class EqualSpacingItemDecoration(private val spacing: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildViewHolder(view).adapterPosition
        val itemCount = state.itemCount

        outRect.left = spacing
        outRect.right = spacing
        outRect.top = spacing
        outRect.bottom = if (position == itemCount - 1) spacing else 0
    }

}
