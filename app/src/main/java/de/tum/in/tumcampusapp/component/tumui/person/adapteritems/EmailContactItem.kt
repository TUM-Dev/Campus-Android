package de.tum.`in`.tumcampusapp.component.tumui.person.adapteritems

import android.content.Intent
import de.tum.`in`.tumcampusapp.R

class EmailContactItem(emailAddress: String) : AbstractContactItem("E-mail", emailAddress, R.drawable.ic_email_black_24dp) {

    override fun getFormattedValue() = value

    override fun getIntent() = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_EMAIL, value)
    }

}