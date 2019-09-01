package de.tum.`in`.tumcampusapp.component.tumui.person.adapteritems

import android.content.Context
import android.content.Intent
import android.net.Uri
import de.tum.`in`.tumcampusapp.R

class MobilePhoneContactItem(text: String) : AbstractContactItem(R.string.mobile_phone, text, R.drawable.ic_outline_phone_24px) {

    override fun getIntent(context: Context): Intent {
        val uri = Uri.parse("tel:$value")
        return Intent(Intent.ACTION_DIAL, uri)
    }
}