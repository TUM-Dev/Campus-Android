package de.tum.`in`.tumcampusapp.component.tumui.person.adapteritems

import android.content.Context
import android.content.Intent
import de.tum.`in`.tumcampusapp.R

class InformationContactItem(text: String) : AbstractContactItem(R.string.additional_info, text, R.drawable.ic_action_info_black) {

    override fun getIntent(context: Context): Intent? = null
}