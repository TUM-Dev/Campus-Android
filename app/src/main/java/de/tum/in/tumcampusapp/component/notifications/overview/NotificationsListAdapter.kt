package de.tum.`in`.tumcampusapp.component.notifications.overview

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.generic.adapter.SimpleStickyListHeadersAdapter

class NotificationsListAdapter(context: Context, results: MutableList<NotificationItemForStickyList>) : SimpleStickyListHeadersAdapter<NotificationItemForStickyList>(context, results) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: ViewHolder
        val view: View

        if (convertView == null) {
            view = inflater.inflate(R.layout.notification_row_item, parent, false)
            holder = ViewHolder()
            holder.textView = view.findViewById(R.id.textView)
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as ViewHolder
        }

        val notification = itemList[position]

        holder.textView?.text = notification.notificationString

        return view
    }

    // the layout of the list
    internal class ViewHolder {
        var textView: TextView? = null
    }

}