package de.tum.`in`.tumcampusapp.component.tumui.person.adapteritems

import android.content.Context
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.generic.activity.BaseNavigationActivity

class RoomContactItem(
    text: String,
    private val roomNumber: String
) : AbstractContactItem(R.string.room, text, R.drawable.ic_outline_business_24px) {

    override fun getIntent(
        context: Context
    ) = BaseNavigationActivity.newRoomFinderIntent(context, roomNumber)

}
