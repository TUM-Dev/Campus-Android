package de.tum.`in`.tumcampusapp.component.ui.overview.card

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.preference.PreferenceManager
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.LinearLayout
import android.widget.RemoteViews
import android.widget.TextView
import de.tum.`in`.tumcampusapp.component.ui.overview.CardManager
import de.tum.`in`.tumcampusapp.utils.Const.CARD_POSITION_PREFERENCE_SUFFIX
import de.tum.`in`.tumcampusapp.utils.Const.DISCARD_SETTINGS_START
import de.tum.`in`.tumcampusapp.utils.Utils

/**
 * Base class for all cards
 * @param type Individual integer for each card type
 * @param context Android Context
 * @param settingsPrefix Preference key prefix used for all preferences belonging to that card
 */
abstract class Card(
        val cardType: Int,
        protected var context: Context,
        val settingsPrefix: String = "",
        showNotificationDefault: Boolean = false) : Comparable<Card> {

    // UI Elements
    protected var mCard: View? = null
    protected var mLinearLayout: LinearLayout? = null
    protected var mTitleView: TextView? = null

    // Settings for showing this card on start page or as notification
    protected var mShowStart = Utils.getSettingBool(context, settingsPrefix + "_start", true)
    protected var mShowPhone = Utils.getSettingBool(context, settingsPrefix + "_phone", showNotificationDefault)

    open fun getId(): Int {
        return 0;
    }

    /**
     * Tells the list adapter and indirectly the SwipeDismissList if the item is dismissible, e.g.: The restore card is not dismissible.
     */
    open val isDismissible: Boolean
        get() = true

    open var position: Int
        get() =
            Utils.getSettingInt(context, "${this.javaClass.simpleName}${CARD_POSITION_PREFERENCE_SUFFIX}", -1)
        set(position) =
            Utils.setSetting(context, "${this.javaClass.simpleName}${CARD_POSITION_PREFERENCE_SUFFIX}", position)


    /**
     * @return Should return the intent that should be launched if the card or the notification gets clicked, null if nothing should happen
     */
    open fun getIntent(): Intent? {
        return null
    }

    /**
     * Updates the Cards content.
     * Override this method, if the card contains any dynamic content, that is not already in its XML
     *
     * @param viewHolder The Card specific view holder
     */
    open fun updateViewHolder(viewHolder: RecyclerView.ViewHolder) {
        context = viewHolder.itemView.context
    }

    /**
     * Adds a new text view to the main card layout
     *
     * @param text Text that should be shown
     * @return Handle to the [TextView]
     */
    protected fun addTextView(text: CharSequence): TextView {
        val textView = TextView(context)
        textView.text = text
        mLinearLayout!!.addView(textView)
        return textView
    }

    /**
     * Should be called after the user has dismissed the card
     */
    fun discardCard() {
        val prefs = context.getSharedPreferences(DISCARD_SETTINGS_START, 0)
        val editor = prefs.edit()
        discard(editor)
        editor.apply()
    }

    /**
     * Returns the Card if it should be displayed in the overview screen or null otherwise.
     *
     * @return The Card to be displayed or null
     */
    open fun getIfShowOnStart(): Card? {
        if (mShowStart) {
            val prefs = context.getSharedPreferences(DISCARD_SETTINGS_START, 0)
            if (shouldShow(prefs)) {
                return this
            }
        }

        return null
    }

    /**
     * Determines if the card should be shown. Decision is based on the given SharedPreferences.
     * This method should be overridden in most cases.
     *
     * @return returns true if the card should be shown
     */
    protected open fun shouldShow(prefs: SharedPreferences): Boolean {
        return true
    }

    /**
     * Sets preferences so that this card does not show up again until
     * reactivated manually by the user
     */
    fun hideAlways() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val e = prefs.edit()
        e.putBoolean(settingsPrefix + "_start", false)
        e.putBoolean(settingsPrefix + "_phone", false)
        e.apply()
    }

    override fun compareTo(other: Card): Int {
        return Integer.compare(position, other.position)
    }

    open fun getRemoteViews(context: Context, appWidgetId: Int): RemoteViews? {
        return null
    }

    /**
     * Save information about the dismissed card/notification to decide later if the card should be shown again
     *
     * @param editor Editor to be used for saving values
     */
    protected abstract fun discard(editor: Editor)

    class DiffCallback(private val oldList: List<Card>,
                       private val newList: List<Card>) : DiffUtil.Callback() {

        override fun getOldListSize() = oldList.size

        override fun getNewListSize() = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                oldList[oldItemPosition].cardType == newList[newItemPosition].cardType

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                oldList[oldItemPosition] == newList[newItemPosition]

    }

}
