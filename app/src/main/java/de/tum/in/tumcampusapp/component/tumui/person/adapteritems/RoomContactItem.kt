package de.tum.`in`.tumcampusapp.component.tumui.person.adapteritems

import android.content.Intent
import de.tum.`in`.tumcampusapp.R

class RoomContactItem(text: String) : AbstractContactItem("Room", text, R.drawable.ic_business_black_24dp) {

    override fun getFormattedValue() = value

    override fun getIntent(): Intent? = null

}