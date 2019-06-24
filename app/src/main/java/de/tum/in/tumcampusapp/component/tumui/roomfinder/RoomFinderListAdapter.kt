package de.tum.`in`.tumcampusapp.component.tumui.roomfinder

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.generic.adapter.SimpleStickyListHeadersAdapter
import de.tum.`in`.tumcampusapp.component.tumui.roomfinder.model.RoomFinderRoom

/**
 * Custom UI adapter for a list of employees.
 */
class RoomFinderListAdapter(context: Context, items: List<RoomFinderRoom>) : SimpleStickyListHeadersAdapter<RoomFinderRoom>(context, items) {

    internal class ViewHolder {
        var tvRoomTitle: TextView? = null
        var tvBuildingTitle: TextView? = null
    }

    override fun getView(position: Int, view: View, parent: ViewGroup): View {
        val holder: ViewHolder
        var convertView: View? = view
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_roomfinder_item, parent, false)

            holder = ViewHolder()
            holder.tvRoomTitle = convertView.findViewById(R.id.startup_actionbar_title)
            holder.tvBuildingTitle = convertView.findViewById(R.id.building)
            convertView.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
        }

        val room = infoList[position]

        // Setting all values in listView
        holder.tvRoomTitle?.text = room.info
        holder.tvBuildingTitle?.text = room.formattedAddress
        return convertView!!
    }
}
