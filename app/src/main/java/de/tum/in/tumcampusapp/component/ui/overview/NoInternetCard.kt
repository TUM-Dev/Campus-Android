package de.tum.`in`.tumcampusapp.component.ui.overview

import android.content.Context
import android.content.SharedPreferences
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.ui.overview.card.CardViewHolder
import de.tum.`in`.tumcampusapp.component.ui.overview.card.StickyCard
import de.tum.`in`.tumcampusapp.service.DownloadWorker
import de.tum.`in`.tumcampusapp.utils.NetUtils
import org.joda.time.DateTime

/**
 * Card that informs that no internet connection is available
 */
class NoInternetCard(context: Context) : StickyCard(CardManager.CARD_NO_INTERNET, context) {

    override fun updateViewHolder(viewHolder: RecyclerView.ViewHolder) {
        super.updateViewHolder(viewHolder)

        val view = viewHolder.itemView
        val lastUpdate = view.findViewById<TextView>(R.id.card_last_update)
        val lastUpdated = DateTime(DownloadWorker.lastUpdate(context))
        val time = DateUtils.getRelativeTimeSpanString(
                lastUpdated.millis,
                System.currentTimeMillis(),
                DateUtils.SECOND_IN_MILLIS).toString()
        lastUpdate.text = String.format(context.getString(R.string.last_updated), time)
    }

    override fun shouldShow(prefs: SharedPreferences) = !NetUtils.isConnected(context)

    override fun getId() = 0

    companion object {

        fun inflateViewHolder(parent: ViewGroup,
                              interactionListener: CardInteractionListener): CardViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.card_no_internet, parent, false)
            return CardViewHolder(view, interactionListener)
        }
    }
}
