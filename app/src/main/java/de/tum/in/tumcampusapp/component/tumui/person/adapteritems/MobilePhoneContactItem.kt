package de.tum.`in`.tumcampusapp.component.tumui.person.adapteritems

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.telephony.PhoneNumberUtils
import de.tum.`in`.tumcampusapp.R
import java.util.*

class MobilePhoneContactItem(text: String) : AbstractContactItem("Mobile Phone", text, R.drawable.ic_phone_black_24dp) {

    override fun getFormattedValue() = PhoneNumberUtils.formatNumber(value, Locale.getDefault().isO3Country) ?: value

    override fun getIntent(context: Context): Intent {
        val uri = Uri.parse("tel:$value")
        return Intent(Intent.ACTION_DIAL, uri)
    }

}