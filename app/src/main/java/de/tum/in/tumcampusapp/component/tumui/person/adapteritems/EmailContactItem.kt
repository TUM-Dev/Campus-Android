package de.tum.`in`.tumcampusapp.component.tumui.person.adapteritems

import android.content.Context
import android.content.Intent
import android.net.Uri
import de.tum.`in`.tumcampusapp.R

class EmailContactItem(emailAddress: String) : AbstractContactItem("E-mail", emailAddress, R.drawable.ic_email_black_24dp) {

    override fun getIntent(context: Context) = Intent(Intent.ACTION_SENDTO).apply {
        type = "text/plain"
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, arrayOf(value))
    }

}