package de.tum.`in`.tumcampusapp.component.ui.updatenote

import android.content.Context
import de.tum.`in`.tumcampusapp.BuildConfig
import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.api.tumonline.CacheControl
import de.tum.`in`.tumcampusapp.component.ui.overview.card.Card
import de.tum.`in`.tumcampusapp.component.ui.overview.card.ProvidesCard
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Utils
import java.io.IOException

class UpdateNoteController(context: Context) : ProvidesCard {
    val mContext = context

    override fun getCards(cacheControl: CacheControl): List<Card> {
        val cards = ArrayList<Card>()
        if (!Utils.getSettingBool(mContext, Const.SHOW_UPDATE_NOTE, false)) {
            return cards;
        }
        try {
            val updateNote = TUMCabeClient.getInstance(mContext)
                    .getUpdateNote(BuildConfig.VERSION_NAME)
            val updateNoteCard = UpdateNoteCard(mContext)
            updateNoteCard.updateNote = updateNote
            cards.add(updateNoteCard)
        } catch (e: IOException) {
            // don't do anything (card is not added)
        }
        return cards
    }
}