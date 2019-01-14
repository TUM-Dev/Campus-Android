package de.tum.`in`.tumcampusapp.component.ui.cafeteria.details

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.FavoriteDishDao
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.CafeteriaMenu
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.CafeteriaPrices
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.utils.splitOnChanged

class CafeteriaMenusAdapter(
        private val context: Context,
        private val isBigLayout: Boolean
) : RecyclerView.Adapter<CafeteriaMenusAdapter.ViewHolder>() {

    private val dao: FavoriteDishDao by lazy {
        TcaDb.getInstance(context).favoriteDishDao()
    }

    private val rolePrices: Map<String, String> by lazy {
        CafeteriaPrices.getRolePrices(context)
    }

    private val itemLayout: Int by lazy {
        if (isBigLayout) R.layout.card_price_line_big else R.layout.card_price_line
    }

    private val adapterItems = mutableListOf<CafeteriaMenuAdapterItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val viewEntity = adapterItems[position]
        viewEntity.bind(holder)
    }

    override fun getItemCount() = adapterItems.size

    fun update(menus: List<CafeteriaMenu>) {
        val newItems = menus
                .splitOnChanged { it.typeShort }
                .map { createAdapterItemsForSection(it) }
                .flatten()
        
        val diffResult = DiffUtil.calculateDiff(DiffUtilCallback(adapterItems, newItems))
        
        adapterItems.clear()
        adapterItems += newItems

        diffResult.dispatchUpdatesTo(this)
    }

    private fun createAdapterItemsForSection(
            menus: List<CafeteriaMenu>
    ): List<CafeteriaMenuAdapterItem> {
        val header = CafeteriaMenuAdapterItem.Header(menus.first())
        val items = menus.map {
            val rolePrice = rolePrices[it.typeLong]
            val isFavorite = dao.checkIfFavoriteDish(it.tag).isNotEmpty()
            CafeteriaMenuAdapterItem.Item(it, isFavorite, rolePrice, isBigLayout, dao)
        }
        return listOf(header) + items
    }

    override fun getItemViewType(position: Int): Int {
        return when (adapterItems[position]) {
            is CafeteriaMenuAdapterItem.Header -> R.layout.card_list_header
            is CafeteriaMenuAdapterItem.Item -> itemLayout
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private class DiffUtilCallback(
            private val oldItems: List<CafeteriaMenuAdapterItem>,
            private val newItems: List<CafeteriaMenuAdapterItem>
    ) : DiffUtil.Callback() {

        override fun getOldListSize() = oldItems.size

        override fun getNewListSize() = newItems.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldItems[oldItemPosition].id == newItems[newItemPosition].id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldItems[oldItemPosition] == newItems[newItemPosition]
        }

    }


}
