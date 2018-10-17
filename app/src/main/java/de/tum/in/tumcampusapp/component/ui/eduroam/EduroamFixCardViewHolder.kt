package de.tum.`in`.tumcampusapp.component.ui.eduroam

import android.content.Context
import android.content.Intent
import android.net.wifi.WifiConfiguration
import android.view.View
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.ui.overview.card.CardViewHolder
import de.tum.`in`.tumcampusapp.utils.Const
import kotlinx.android.synthetic.main.card_eduroam_fix.view.*
import org.jetbrains.anko.wifiManager

class EduroamFixCardViewHolder(itemView: View) : CardViewHolder(itemView) {

    fun bind(eduroam: WifiConfiguration?, errors: List<String>) = with(itemView) {
        if (errors.isNotEmpty()) {
            eduroam_errors.visibility = View.VISIBLE
            eduroam_errors.text = errors.joinToString("\n")
        }

        eduroam_action_button.setOnClickListener {
            performEduroamFix(context, eduroam)
        }

        if (errors.size == 1 && errors.first() == context.getString(R.string.wifi_identity_zone)) {
            eduroam_insecure_message.visibility = View.GONE
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
