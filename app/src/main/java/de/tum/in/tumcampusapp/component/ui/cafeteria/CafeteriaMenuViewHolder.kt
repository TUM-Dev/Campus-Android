package de.tum.`in`.tumcampusapp.component.ui.cafeteria

import android.view.View
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.details.CafeteriaDetailsSectionFragment
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.CafeteriaWithMenus
import de.tum.`in`.tumcampusapp.component.ui.overview.card.CardViewHolder
import kotlinx.android.synthetic.main.card_cafeteria_menu.view.*
import org.joda.time.format.DateTimeFormat

class CafeteriaMenuViewHolder(itemView: View) : CardViewHolder(itemView) {

    private var didBind = false

    fun bind(cafeteria: CafeteriaWithMenus) {
        with(itemView) {
            cafeteriaNameTextView.text = cafeteria.name
            menuDateTextView.text = DateTimeFormat.mediumDate().print(cafeteria.nextMenuDate)

            if (!didBind) {
                CafeteriaDetailsSectionFragment.showMenu(contentContainerLayout,
                        cafeteria.id, cafeteria.nextMenuDate, false, cafeteria.menus)
                didBind = true
            }
        }
    }

}