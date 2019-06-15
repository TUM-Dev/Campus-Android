package de.tum.`in`.tumcampusapp.component.tumui.tutionfees

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.navigation.NavDestination
import de.tum.`in`.tumcampusapp.component.tumui.tutionfees.model.Tuition
import de.tum.`in`.tumcampusapp.component.ui.overview.CardInteractionListener
import de.tum.`in`.tumcampusapp.component.ui.overview.CardManager
import de.tum.`in`.tumcampusapp.component.ui.overview.card.Card
import de.tum.`in`.tumcampusapp.component.ui.overview.card.CardViewHolder
import de.tum.`in`.tumcampusapp.utils.DateTimeUtils
import org.joda.time.format.DateTimeFormat

/**
 * Card that shows information about your fees that have to be paid or have been paid
 */
class TuitionFeesCard(
        context: Context,
        private val tuition: Tuition
) : Card(CardManager.CARD_TUITION_FEE, context, "card_tuition_fee") {

    override val optionsMenuResId: Int
        get() = R.menu.card_popup_menu

    val title: String
        get() = context.getString(R.string.tuition_fees)

    override fun getId(): Int {
        return 0
    }

    override fun getNavigationDestination(): NavDestination {
        return NavDestination.Fragment(TuitionFeesFragment::class.java)
    }

    override fun updateViewHolder(viewHolder: RecyclerView.ViewHolder) {
        super.updateViewHolder(viewHolder)

        val reregisterInfoTextView = viewHolder.itemView.findViewById<TextView>(R.id.reregister_info_text_view)
        val outstandingBalanceTextView = viewHolder.itemView.findViewById<TextView>(R.id.outstanding_balance_text_view)

        if (tuition.isPaid) {
            val placeholderText = context.getString(R.string.reregister_success)
            val text = String.format(placeholderText, tuition.semester)
            reregisterInfoTextView.text = text
        } else {
            val date = tuition.deadline
            val dateText = DateTimeFormat.mediumDate().print(date)

            val text = String.format(context.getString(R.string.reregister_todo), dateText)
            reregisterInfoTextView.text = text

            val textWithPlaceholder = context.getString(R.string.amount_dots_card)
            val balanceText = String.format(textWithPlaceholder, tuition.getAmountText(context))
            outstandingBalanceTextView.text = balanceText
            outstandingBalanceTextView.visibility = View.VISIBLE
        }
    }

    override fun shouldShow(prefs: SharedPreferences): Boolean {
        val prevDeadline = prefs.getString(LAST_FEE_FRIST, "")!!
        val prevAmount = prefs.getString(LAST_FEE_SOLL, java.lang.Float.toString(tuition.amount))!!

        // If app gets started for the first time and fee is already paid don't annoy user
        // by showing him that he has been re-registered successfully
        val deadline = DateTimeUtils.getDateString(tuition.deadline)
        val amount = java.lang.Float.toString(tuition.amount)
        return !(prevDeadline.isEmpty() && tuition.isPaid) && (prevDeadline < deadline || prevAmount > amount)
    }

    public override fun discard(editor: Editor) {
        val deadline = DateTimeUtils.getDateString(tuition.deadline)
        val amount = java.lang.Float.toString(tuition.amount)
        editor.putString(LAST_FEE_FRIST, deadline)
        editor.putString(LAST_FEE_SOLL, amount)
    }

    companion object {
        private const val LAST_FEE_FRIST = "fee_frist"
        private const val LAST_FEE_SOLL = "fee_soll"

        @JvmStatic
        fun inflateViewHolder(parent: ViewGroup, interactionListener: CardInteractionListener): CardViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.card_tuition_fees, parent, false)
            return CardViewHolder(view, interactionListener)
        }
    }

}
