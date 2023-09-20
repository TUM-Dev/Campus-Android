package de.tum.`in`.tumcampusapp.component.ui.overview.card

import android.content.Context
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import de.tum.`in`.tumcampusapp.component.ui.overview.CardInteractionListener

open class CardViewHolder @JvmOverloads constructor(
    itemView: View,
    private val listener: CardInteractionListener? = null
) : RecyclerView.ViewHolder(itemView) {

    var currentCard: Card? = null

    private val context: Context by lazy { itemView.context }

    protected val activity: AppCompatActivity by lazy { context as AppCompatActivity }
}
