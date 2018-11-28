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
import de.tum.`in`.tumcampusapp.component.ui.overview.CardManager
import de.tum.`in`.tumcampusapp.component.ui.overview.card.Card
import de.tum.`in`.tumcampusapp.component.ui.overview.card.CardViewHolder
import de.tum.`in`.tumcampusapp.component.ui.updatenote.model.UpdateNote
import de.tum.`in`.tumcampusapp.utils.Const

class UpdateNoteCard(context: Context) : Card(CardManager.CARD_UPDATE_NOTE, context, "whats_new") {
    var updateNote: UpdateNote? = null

    override fun updateViewHolder(viewHolder: RecyclerView.ViewHolder) {
        super.updateViewHolder(viewHolder)
        val version = BuildConfig.VERSION_NAME
        (viewHolder as UpdateNoteViewHolder).bind(updateNote, version)
    }

    override fun shouldShow(prefs: SharedPreferences): Boolean {
        return prefs.getBoolean(Const.SHOW_UPDATE_NOTE, false)
    }

    override fun discard(editor: SharedPreferences.Editor) {
        editor.putBoolean(Const.SHOW_UPDATE_NOTE, false)
    }

    companion object {
        @JvmStatic
        fun inflateViewHolder(parent: ViewGroup): CardViewHolder {
            val card = LayoutInflater.from(parent.context)
                    .inflate(R.layout.card_update_note, parent, false)
            return UpdateNoteViewHolder(card)
        }
    }

    class UpdateNoteViewHolder(view: View) : CardViewHolder(view) {
        internal var titleView: TextView = view.findViewById(R.id.update_note_title)
        internal var subtitleView: TextView = view.findViewById(R.id.update_note_subtitle)
        internal var messageView: TextView = view.findViewById(R.id.update_note_message)

        fun bind(updateNote: UpdateNote?, version: String) {
            subtitleView.text = activity.getString(R.string.update_note_version, version)
            messageView.text = updateNote?.updateNote
        }
    }
}
