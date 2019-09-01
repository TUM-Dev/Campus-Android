package de.tum.`in`.tumcampusapp.component.ui.updatenote

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.tum.`in`.tumcampusapp.BuildConfig
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.ui.overview.CardInteractionListener
import de.tum.`in`.tumcampusapp.component.ui.overview.CardManager
import de.tum.`in`.tumcampusapp.component.ui.overview.card.Card
import de.tum.`in`.tumcampusapp.component.ui.overview.card.CardViewHolder
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Utils

class UpdateNoteCard(context: Context) : Card(CardManager.CARD_UPDATE_NOTE, context, "update_note") {

    override fun updateViewHolder(viewHolder: RecyclerView.ViewHolder) {
        super.updateViewHolder(viewHolder)
        val version = BuildConfig.VERSION_NAME
        val updateMessage = Utils.getSetting(context, Const.UPDATE_MESSAGE, "")
        (viewHolder as UpdateNoteViewHolder).bind(updateMessage, version)
    }

    override fun shouldShow(prefs: SharedPreferences): Boolean {
        return Utils.getSettingBool(context, Const.SHOW_UPDATE_NOTE, false) &&
        Utils.getSetting(context, Const.UPDATE_MESSAGE, "").isNotEmpty()
    }

    override fun discard(editor: SharedPreferences.Editor) {
        Utils.setSetting(context, Const.SHOW_UPDATE_NOTE, false)
    }

    companion object {
        @JvmStatic
        fun inflateViewHolder(
            parent: ViewGroup,
            interactionListener: CardInteractionListener
        ): CardViewHolder {
            val card = LayoutInflater.from(parent.context)
                    .inflate(R.layout.card_update_note, parent, false)
            return UpdateNoteViewHolder(card, interactionListener)
        }
    }

    class UpdateNoteViewHolder(
        view: View,
        interactionListener: CardInteractionListener
    ) : CardViewHolder(view, interactionListener) {
        internal var subtitleView: TextView = view.findViewById(R.id.update_note_subtitle)
        internal var messageView: TextView = view.findViewById(R.id.update_note_message)

        fun bind(updateMessage: String, version: String) {
            subtitleView.text = activity.getString(R.string.update_note_version, version)
            messageView.text = updateMessage
        }
    }
}
