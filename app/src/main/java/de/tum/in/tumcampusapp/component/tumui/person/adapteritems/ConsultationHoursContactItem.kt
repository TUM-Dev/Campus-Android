package de.tum.`in`.tumcampusapp.component.tumui.person.adapteritems

import android.content.Context
import android.content.Intent
import de.tum.`in`.tumcampusapp.R

class ConsultationHoursContactItem(text: String) : AbstractContactItem(
        "Consultation Hours", text, R.drawable.ic_access_time_black_24dp) {

    override fun getIntent(context: Context): Intent? = null

}