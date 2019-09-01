package de.tum.`in`.tumcampusapp.component.ui.cafeteria

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.navigation.NavDestination
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.fragment.CafeteriaFragment
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.CafeteriaWithMenus
import de.tum.`in`.tumcampusapp.component.ui.overview.CardInteractionListener
import de.tum.`in`.tumcampusapp.component.ui.overview.CardManager
import de.tum.`in`.tumcampusapp.component.ui.overview.card.Card
import de.tum.`in`.tumcampusapp.component.ui.overview.card.CardViewHolder
import de.tum.`in`.tumcampusapp.utils.Const
import java.util.*

/**
 * Card that shows the cafeteria menu
 */
class CafeteriaMenuCard(context: Context, private val cafeteria: CafeteriaWithMenus) : Card(CardManager.CARD_CAFETERIA, context, "card_cafeteria") {

    override val optionsMenuResId: Int
        get() = R.menu.card_popup_menu

    val title: String?
        get() = cafeteria.name

    override fun updateViewHolder(viewHolder: RecyclerView.ViewHolder) {
        super.updateViewHolder(viewHolder)
        if (viewHolder is CafeteriaMenuViewHolder) {
            viewHolder.bind(cafeteria)
        }
    }

    override fun getNavigationDestination(): NavDestination? {
        val bundle = Bundle()
        bundle.putInt(Const.CAFETERIA_ID, cafeteria.id)
        return NavDestination.Fragment(CafeteriaFragment::class.java, bundle)
    }

    public override fun discard(editor: Editor) {
        val date = cafeteria.nextMenuDate
        editor.putLong(CAFETERIA_DATE + "_" + cafeteria.id, date.millis)
    }

    override fun shouldShow(prefs: SharedPreferences): Boolean {
        // the card reappears when the day is over and a new menu will be shown
        val prevDate = prefs.getLong(CAFETERIA_DATE + "_" + cafeteria.id, 0)
        val date = cafeteria.nextMenuDate
        return prevDate < date.millis
    }

    override fun hideAlways() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val id = cafeteria.id.toString()
        val ids = prefs.getStringSet(Const.CAFETERIA_CARDS_SETTING, HashSet())!!
        if (ids.contains(id)) {
            ids.remove(id)
        } else {
            ids.remove(Const.CAFETERIA_BY_LOCATION_SETTINGS_ID)
        }
        prefs.edit().putStringSet(Const.CAFETERIA_CARDS_SETTING, ids).apply()
    }

    companion object {
        private const val CAFETERIA_DATE = "cafeteria_date"

        @JvmStatic
        fun inflateViewHolder(parent: ViewGroup, interactionListener: CardInteractionListener): CardViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.card_cafeteria_menu, parent, false)
            return CafeteriaMenuViewHolder(view, interactionListener)
        }
    }
}
