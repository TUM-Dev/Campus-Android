package de.tum.`in`.tumcampusapp.component.ui.overview.card

import android.content.Context
import android.content.SharedPreferences

abstract class StickyCard(cardType: Int, context: Context) : Card(cardType, context) {

    override val isDismissible: Boolean
        get() = false

    override fun discard(editor: SharedPreferences.Editor) {
        // Sticky cards can't be dismissed
    }
}
