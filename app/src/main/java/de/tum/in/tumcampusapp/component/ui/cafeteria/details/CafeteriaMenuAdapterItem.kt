package de.tum.`in`.tumcampusapp.component.ui.cafeteria.details

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.CafeteriaMenuFormatter
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.FavoriteDishDao
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.CafeteriaMenu
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.FavoriteDish
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

sealed class CafeteriaMenuAdapterItem {

    abstract val id: String

    abstract fun bind(
            holder: CafeteriaMenusAdapter.ViewHolder,
            listener: (() -> Unit)?
    )

    data class Header(val menu: CafeteriaMenu) : CafeteriaMenuAdapterItem() {

        override val id: String
            get() = "header_${menu.menuId}"

        override fun bind(
                holder: CafeteriaMenusAdapter.ViewHolder,
                listener: (() -> Unit)?
        ) = with(holder.itemView) {
            val headerTextView = findViewById<TextView>(R.id.headerTextView)

            headerTextView.text = menu.dishType.replace("[0-9]", "").trim()
            setOnClickListener { listener?.invoke() }
        }
    }

    data class Item(
            val menu: CafeteriaMenu,
            val isFavorite: Boolean = false,
            val rolePrice: String? = null,
            val isBigLayout: Boolean,
            val favoriteDishDao: FavoriteDishDao
    ) : CafeteriaMenuAdapterItem() {

        override val id: String
            get() = "item_${menu.menuId}"

        override fun bind(
                holder: CafeteriaMenusAdapter.ViewHolder,
                listener: (() -> Unit)?
        ) = with(holder.itemView) {
            val formatter = CafeteriaMenuFormatter(context)
            val menuSpan = formatter.format(menu, isBigLayout)

            val nameTextView = findViewById<TextView>(R.id.nameTextView)
            nameTextView.text = menuSpan

            setOnClickListener { listener?.invoke() }
            rolePrice?.let { showPrice(this) } ?: hidePrice(this)
            hideFavorite(this)
        }

        private fun showPrice(
                itemView: View,
        ) = with(itemView) {
            val priceTextView = findViewById<TextView>(R.id.priceTextView)
            priceTextView.text = menu.getPriceText(context)
        }

        private fun showFavorite(itemView: View) = with(itemView) {
            val favoriteDish = findViewById<ImageView>(R.id.favoriteDish)
            favoriteDish.isSelected = isFavorite
            favoriteDish.setOnClickListener { view ->
                if (!view.isSelected) {
                    val formatter = DateTimeFormat.forPattern("dd-MM-yyyy")
                    val date = formatter.print(DateTime.now())
                    favoriteDishDao.insertFavouriteDish(FavoriteDish.create(menu, date))
                    view.isSelected = true
                } else {
                    //favoriteDishDao.deleteFavoriteDish(menu.cafeteriaId, menu.name)
                    view.isSelected = false
                }
            }
        }

        private fun hidePrice(itemView: View) = with(itemView) {
            val priceTextView = findViewById<TextView>(R.id.priceTextView)
            priceTextView.visibility = View.GONE
        }

        private fun hideFavorite(itemView: View) = with(itemView) {
            val favoriteDish = findViewById<ImageView>(R.id.favoriteDish)
            favoriteDish.visibility = View.GONE
        }
    }
}
