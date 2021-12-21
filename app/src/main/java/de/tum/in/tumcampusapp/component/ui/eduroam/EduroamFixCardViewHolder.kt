package de.tum.`in`.tumcampusapp.component.ui.eduroam

import android.content.Context
import android.content.Intent
import android.net.wifi.WifiConfiguration
import android.view.View
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.ui.overview.CardInteractionListener
import de.tum.`in`.tumcampusapp.component.ui.overview.card.CardViewHolder
import de.tum.`in`.tumcampusapp.utils.Const
import org.jetbrains.anko.wifiManager

class EduroamFixCardViewHolder(
    itemView: View,
    interactionListener: CardInteractionListener
) : CardViewHolder(itemView, interactionListener) {

    private val eduroamErrors = itemView.findViewById<TextView>(R.id.eduroam_errors)
    private val eduroamActionButton = itemView.findViewById<MaterialButton>(R.id.eduroam_action_button)
    private val eduroamInsecureMessage = itemView.findViewById<TextView>(R.id.eduroam_insecure_message)

    fun bind(eduroam: WifiConfiguration?, errors: List<String>) = with(itemView) {
        if (errors.isNotEmpty()) {
            eduroamErrors.visibility = View.VISIBLE
            eduroamErrors.text = errors.joinToString("\n")
        }

        eduroamActionButton.setOnClickListener {
            performEduroamFix(context, eduroam)
        }

        if (errors.size == 1 && errors.first() == context.getString(R.string.wifi_identity_zone)) {
            eduroamInsecureMessage.visibility = View.GONE
        }
    }

    private fun performEduroamFix(context: Context, eduroam: WifiConfiguration?) {
        eduroam?.let {
            context.wifiManager.removeNetwork(it.networkId)
        }

        val intent = Intent(context, SetupEduroamActivity::class.java)
        // TCA should only produce correct profiles, so incorrect ones were configured somewhere else
        intent.putExtra(Const.EXTRA_FOREIGN_CONFIGURATION_EXISTS, true)
        context.startActivity(intent)
    }
}
