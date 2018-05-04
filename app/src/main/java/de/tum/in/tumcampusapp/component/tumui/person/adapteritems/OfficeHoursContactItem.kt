package de.tum.`in`.tumcampusapp.component.tumui.person.adapteritems

import android.content.Context
import android.content.Intent
import de.tum.`in`.tumcampusapp.R

class OfficeHoursContactItem(text: String) : AbstractContactItem(R.string.office_hours, text, R.drawable.ic_access_time_black_24dp) {

    override fun getIntent(context: Context): Intent? = null

}