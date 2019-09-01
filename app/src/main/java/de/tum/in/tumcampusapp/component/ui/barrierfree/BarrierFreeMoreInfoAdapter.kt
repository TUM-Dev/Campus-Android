package de.tum.`in`.tumcampusapp.component.ui.barrierfree

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.generic.adapter.SimpleStickyListHeadersAdapter
import de.tum.`in`.tumcampusapp.component.ui.barrierfree.model.BarrierFreeMoreInfo

class BarrierFreeMoreInfoAdapter(context: Context, infos: List<BarrierFreeMoreInfo>) :
        SimpleStickyListHeadersAdapter<BarrierFreeMoreInfo>(context, infos.toMutableList()) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: ViewHolder
        val view: View
        if (convertView == null) {
            view = inflater.inflate(R.layout.activity_barrier_free_more_info_listview, parent, false)

            // Crate UI element
            holder = ViewHolder()
            holder.title = view.findViewById(R.id.barrierfreeMoreInfoTitle)
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as ViewHolder
        }

        // set title
        val info = itemList[position]
        holder.title?.text = info.title

        return view
    }

    // the layout of the list
    internal class ViewHolder {
        var title: TextView? = null
    }
}