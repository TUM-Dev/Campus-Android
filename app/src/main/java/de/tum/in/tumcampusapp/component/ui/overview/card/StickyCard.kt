package de.tum.`in`.tumcampusapp.component.ui.overview.card

import android.content.Context
import android.content.SharedPreferences
import de.tum.`in`.tumcampusapp.component.ui.overview.CardManager

abstract class StickyCard(cardType: CardManager.CardTypes, context: Context) : Card(cardType, context) {

    override val isDismissible: Boolean
        get() = false

    override fun discard(editor: SharedPreferences.Editor) {
        // Sticky cards can't be dismissed
    }
}
