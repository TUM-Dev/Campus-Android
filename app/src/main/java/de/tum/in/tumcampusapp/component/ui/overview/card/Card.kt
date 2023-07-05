package de.tum.`in`.tumcampusapp.component.ui.overview.card

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import androidx.recyclerview.widget.DiffUtil
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.navigation.NavDestination
import de.tum.`in`.tumcampusapp.component.ui.overview.CardManager
import de.tum.`in`.tumcampusapp.utils.Const.CARD_POSITION_PREFERENCE_SUFFIX
import de.tum.`in`.tumcampusapp.utils.Utils
import org.jetbrains.anko.defaultSharedPreferences

/**
 * Base class for all cards
 * @param cardType Individual integer for each card type
 * @param context Android Context
 * @param settingsPrefix Preference key prefix used for all preferences belonging to that card
 */
abstract class Card(
    val cardType: CardManager.CardTypes,
    protected var context: Context,
    val settingsPrefix: String = ""
) : Comparable<Card> {

    // stores information for dismiss
    private val dismissCardSharedPreferences: SharedPreferences = context.getSharedPreferences("CardPref$cardType", Context.MODE_PRIVATE)

    open fun getId(): Int {
        return 0
    }

    /**
     * Tells the list adapter and indirectly the SwipeDismissList if the item is dismissible.
     * E.g.: The restore card is not dismissible.
     */
    open val isDismissible: Boolean
        get() = true

    /**
     * The options menu that should be inflated when the user presses the options icon in a card.
     */
    open val optionsMenuResId: Int
        get() = R.menu.card_popup_menu_no_settings

    open var position: Int
        get() =
            Utils.getSettingInt(context, "${this.javaClass.simpleName}$CARD_POSITION_PREFERENCE_SUFFIX", -1)
        set(position) =
            Utils.setSetting(context, "${this.javaClass.simpleName}$CARD_POSITION_PREFERENCE_SUFFIX", position)

    /**
     * Returns the [NavDestination] when the card is clicked, or null if nothing should happen
     */
    open fun getNavigationDestination(): NavDestination? {
        return null
    }

    /**
     * Updates the Cards content.
     * Override this method, if the card contains any dynamic content, that is not already in its XML
     *
     * @param viewHolder The Card specific view holder
     */
    open fun updateViewHolder(viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder) {
        context = viewHolder.itemView.context
    }

    /**
     * Should be called after the user has dismissed the card
     */
    fun discard() {
        //val prefs = context.getSharedPreferences(DISCARD_SETTINGS_START, 0)
        val editor = dismissCardSharedPreferences.edit()
        discard(editor)
        editor.apply()
    }

    /**
     * Returns the Card if it should be displayed in the overview screen or null otherwise.
     *
     * @return The Card to be displayed or null
     */
    open fun getIfShowOnStart(): Card? {
        if (context.defaultSharedPreferences.getBoolean(context.getString(cardType.showCardPreferenceStringRes), true)) {
            if (shouldShow(dismissCardSharedPreferences)) {
                return this
            }
        }
        return null
    }

    /**
     * Determines if the card should be shown at the card level. Decision is based on the given SharedPreferences.
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
    open fun hideAlways() {
        context.defaultSharedPreferences
            .edit()
            .putBoolean(context.getString(cardType.showCardPreferenceStringRes), false)
            .apply()
        Utils.log("Hiding card: $cardType")
        println()
    }

    override fun compareTo(other: Card): Int {
        return position.compareTo(other.position)
    }

    /**
     * Save information about the dismissed card/notification to decide later if the cardView should be shown again
     *
     * @param editor Editor to be used for saving values
     */
    protected abstract fun discard(editor: Editor)

    class DiffCallback(
        private val oldList: List<Card>,
        private val newList: List<Card>
    ) : DiffUtil.Callback() {

        override fun getOldListSize() = oldList.size

        override fun getNewListSize() = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                oldList[oldItemPosition].cardType == newList[newItemPosition].cardType

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                oldList[oldItemPosition] == newList[newItemPosition]
    }
}
