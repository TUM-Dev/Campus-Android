package de.tum.`in`.tumcampusapp.component.tumui.person.adapteritems

import android.content.Context
import android.content.Intent
import de.tum.`in`.tumcampusapp.R

class OfficeHoursContactItem(text: String) : AbstractContactItem(R.string.office_hours, text, R.drawable.ic_outline_access_time_24px) {

    override fun getIntent(context: Context): Intent? = null
}