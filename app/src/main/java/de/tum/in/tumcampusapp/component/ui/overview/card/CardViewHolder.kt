package de.tum.`in`.tumcampusapp.component.ui.overview.card

import android.app.Activity
import android.content.Intent
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.content.ContextCompat
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.settings.UserPreferencesActivity
import de.tum.`in`.tumcampusapp.utils.Const

open class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener, View.OnLongClickListener, PopupMenu.OnMenuItemClickListener {

    var currentCard: Card? = null
    private val activity: Activity

    init {
        itemView.setOnClickListener(this)
        itemView.setOnLongClickListener(this)
        activity = itemView.context as Activity
    }

    override fun onClick(v: View) {
        val intent = currentCard?.getIntent() ?: return
        val transitionName = activity.getString(R.string.transition_card)

        val options = ActivityOptionsCompat
                .makeSceneTransitionAnimation(activity, v, transitionName)
        ContextCompat.startActivity(activity, intent, options.toBundle())
    }

    override fun onLongClick(v: View): Boolean {
        PopupMenu(v.context, v, Gravity.CENTER_HORIZONTAL).apply {
            menuInflater.inflate(R.menu.card_popup_menu, menu)
            setOnMenuItemClickListener(this@CardViewHolder)
            show()
        }
        return true
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.open_card_setting -> handleOpenCardSettings()
            R.id.always_hide_card -> handleAlwaysHideCard()
            else -> false
        }
    }

    private fun handleOpenCardSettings(): Boolean {
        val key = currentCard?.settingsPrefix ?: return true

        val intent = Intent(itemView.context, UserPreferencesActivity::class.java).apply {
            putExtra(Const.PREFERENCE_SCREEN, key)
        }
        itemView.context.startActivity(intent)
        return true
    }

    private fun handleAlwaysHideCard(): Boolean {
        currentCard?.hideAlways()
        currentCard?.discardCard()
        return true
    }

}