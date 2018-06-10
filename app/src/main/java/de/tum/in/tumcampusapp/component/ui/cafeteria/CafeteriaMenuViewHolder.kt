package de.tum.`in`.tumcampusapp.component.ui.cafeteria

import android.view.View
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.details.CafeteriaDetailsSectionFragment
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.CafeteriaMenu
import de.tum.`in`.tumcampusapp.component.ui.overview.card.CardViewHolder
import kotlinx.android.synthetic.main.card_cafeteria_menu.view.*
import java.text.DateFormat
import java.util.*

class CafeteriaMenuViewHolder(itemView: View) : CardViewHolder(itemView) {

    // TODO Till: Use CafeteriaWithMenus once available from other branch

    private var didBind = false

    fun bind(id: Int, name: String, date: Date, dateStr: String, menus: List<CafeteriaMenu>) {
        with(itemView) {
            cafeteriaNameTextView.text = name
            menuDateTextView.text = DateFormat.getDateInstance().format(date)

            if (!didBind) {
                CafeteriaDetailsSectionFragment
                        .showMenu(contentContainerLayout, id, dateStr, false, menus)
                didBind = true
            }
        }
    }

}