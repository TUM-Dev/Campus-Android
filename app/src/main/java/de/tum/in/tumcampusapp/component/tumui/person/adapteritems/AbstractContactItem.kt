package de.tum.`in`.tumcampusapp.component.tumui.person.adapteritems

import android.content.Context
import android.content.Intent

abstract class AbstractContactItem(val labelResourceId: Int, val value: String, val iconResourceId: Int) {

    abstract fun getIntent(context: Context): Intent?

}