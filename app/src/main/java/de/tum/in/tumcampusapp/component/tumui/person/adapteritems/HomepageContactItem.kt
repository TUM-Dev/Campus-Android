package de.tum.`in`.tumcampusapp.component.tumui.person.adapteritems

import android.content.Context
import android.content.Intent
import android.net.Uri
import de.tum.`in`.tumcampusapp.R

class HomepageContactItem(url: String) : AbstractContactItem(R.string.homepage, url, R.drawable.ic_outline_public_24px) {

    override fun getIntent(context: Context) = Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse(value)
    }

}