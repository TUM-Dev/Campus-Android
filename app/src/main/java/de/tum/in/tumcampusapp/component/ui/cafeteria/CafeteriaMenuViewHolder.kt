package de.tum.`in`.tumcampusapp.component.ui.cafeteria

import android.view.View
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.details.CafeteriaDetailsSectionFragment
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.CafeteriaWithMenus
import de.tum.`in`.tumcampusapp.component.ui.overview.card.CardViewHolder
import java.text.DateFormat

class CafeteriaMenuViewHolder(itemView: View) : CardViewHolder(itemView) {

    private var didBind = false

    fun bind(cafeteria: CafeteriaWithMenus) {
        with(itemView) {
            cafeteriaNameTextView.text = cafeteria.name
            menuDateTextView.text = DateFormat.getDateInstance().format(cafeteria.nextMenuDate)

            if (!didBind) {
                CafeteriaDetailsSectionFragment.showMenu(contentContainerLayout,
                        cafeteria.id, cafeteria.nextMenuDate, false, cafeteria.menus)
                didBind = true
            }
        }
    }

}