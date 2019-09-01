package de.tum.`in`.tumcampusapp.component.ui.news

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView

import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.navigation.NavDestination
import de.tum.`in`.tumcampusapp.component.ui.news.model.NewsAlert
import de.tum.`in`.tumcampusapp.component.ui.overview.CardInteractionListener
import de.tum.`in`.tumcampusapp.component.ui.overview.CardManager
import de.tum.`in`.tumcampusapp.component.ui.overview.card.Card
import de.tum.`in`.tumcampusapp.component.ui.overview.card.CardViewHolder
import org.jetbrains.anko.defaultSharedPreferences

/**
 * Shows important news
 */
class TopNewsCard(context: Context) : Card(CardManager.CARD_TOP_NEWS, context, "top_news") {
    private lateinit var imageView: ImageView
    private lateinit var progress: ProgressBar
    private val topNewsStore: TopNewsStore
    private val newsAlert: NewsAlert?

    init {
        this.topNewsStore = RealTopNewsStore(context.defaultSharedPreferences)
        this.newsAlert = topNewsStore.getNewsAlert()
    }

    private fun updateImageView() {
        if (newsAlert == null || newsAlert.url.isEmpty()) {
            return
        }

        Picasso.get()
                .load(newsAlert.url)
                .into(imageView, object : Callback {
                    override fun onSuccess() {
                        // remove progress bar
                        progress.visibility = View.GONE
                    }
                    override fun onError(e: Exception) {
                        discard()
                    }
                })
    }

    override fun getId(): Int {
        return 0
    }

    override fun getNavigationDestination(): NavDestination? {
        return if (newsAlert == null || newsAlert.link.isEmpty()) {
            null
        } else NavDestination.Link(newsAlert.link)
    }

    override fun updateViewHolder(viewHolder: RecyclerView.ViewHolder) {
        super.updateViewHolder(viewHolder)
        imageView = viewHolder.itemView.findViewById(R.id.top_news_img)
        progress = viewHolder.itemView.findViewById(R.id.top_news_progress)
        updateImageView()
    }

    override fun shouldShow(prefs: SharedPreferences): Boolean {
        return if (newsAlert == null) {
            false
        } else topNewsStore.isEnabled() && newsAlert.shouldDisplay
    }

    public override fun discard(editor: SharedPreferences.Editor) {
        topNewsStore.setEnabled(false)
    }

    companion object {
        @JvmStatic
        fun inflateViewHolder(parent: ViewGroup, interactionListener: CardInteractionListener): CardViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.card_top_news, parent, false)
            return CardViewHolder(view, interactionListener)
        }
    }
}
