package de.tum.`in`.tumcampusapp.component.ui.overview.card

import android.app.Activity
import android.content.Intent
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.content.ContextCompat
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View

import java.util.ArrayList

import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.settings.UserPreferencesActivity
import de.tum.`in`.tumcampusapp.utils.Const

open class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener, PopupMenu.OnMenuItemClickListener {
    var currentCard: Card? = null
    var addedViews: MutableList<View> = ArrayList()
    private val mActivity: Activity

    init {
        itemView.setOnClickListener(this)
        itemView.setOnLongClickListener(this)
        mActivity = itemView.context as Activity
    }

    override fun onClick(v: View) {
        val i = currentCard!!.intent
        val transitionName = mActivity.getString(R.string.transition_card)
        if (i != null) {
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    mActivity, v, transitionName
            )
            ContextCompat.startActivity(mActivity, i, options.toBundle())
        }
    }

    override fun onLongClick(v: View): Boolean {
        val key = currentCard!!.settings ?: return false
        val menu = PopupMenu(v.context, v, Gravity.CENTER_HORIZONTAL)
        val inf = menu.menuInflater
        inf.inflate(R.menu.card_popup_menu, menu.menu)
        menu.setOnMenuItemClickListener(this)

        menu.show()
        return true
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        val i = item.itemId
        if (i == R.id.open_card_setting) {// Open card's preference screen
            val key = currentCard!!.settings ?: return true

            val intent = Intent(itemView.context, UserPreferencesActivity::class.java)
            intent.putExtra(Const.PREFERENCE_SCREEN, key)
            itemView.context
                    .startActivity(intent)
            return true
        } else if (i == R.id.always_hide_card) {
            currentCard!!.hideAlways()
            currentCard!!.discardCard()
            return true
        } else {
            return false
        }
    }
}