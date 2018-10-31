package de.tum.`in`.tumcampusapp.component.other.generic.adapter

import android.graphics.Rect
import androidx.recyclerview.widget.RecyclerView
import android.view.View

class GridEqualSpacingDecoration(
        private val spacing: Int,
        private val spanCount: Int
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)
        val column = position % spanCount

        outRect.left = spacing - column * spacing / spanCount
        outRect.right = (column + 1) * spacing / spanCount

        if (position < spanCount) {
            outRect.top = spacing
        }
        outRect.bottom = spacing
    }

}