package de.tum.`in`.tumcampusapp.component.tumui.person.adapteritems

import android.content.Context
import android.content.Intent
import android.net.Uri
import de.tum.`in`.tumcampusapp.R

class WebsiteContactItem(url: String) : AbstractContactItem("Website", url, R.drawable.ic_public_black_24dp) {

    override fun getIntent(context: Context) = Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse(value)
    }

}