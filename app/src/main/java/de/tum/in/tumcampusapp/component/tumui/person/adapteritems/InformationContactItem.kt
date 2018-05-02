package de.tum.`in`.tumcampusapp.component.tumui.person.adapteritems

import android.content.Context
import android.content.Intent
import de.tum.`in`.tumcampusapp.R

class InformationContactItem(text: String) : AbstractContactItem("Additional Info", text, R.drawable.ic_info_black_24dp) {

    override fun getFormattedValue() = value

    override fun getIntent(context: Context): Intent? = null

}