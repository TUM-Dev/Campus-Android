package de.tum.`in`.tumcampusapp.component.ui.eduroam

import android.content.Context
import android.content.SharedPreferences
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiEnterpriseConfig.Eap.PEAP
import android.net.wifi.WifiEnterpriseConfig.Eap.TTLS
import android.net.wifi.WifiEnterpriseConfig.Phase2.MSCHAPV2
import android.net.wifi.WifiEnterpriseConfig.Phase2.PAP
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.M
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.ui.overview.CardInteractionListener
import de.tum.`in`.tumcampusapp.component.ui.overview.CardManager
import de.tum.`in`.tumcampusapp.component.ui.overview.card.Card
import de.tum.`in`.tumcampusapp.component.ui.overview.card.CardViewHolder
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Utils
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.wifiManager
import java.util.*
import java.util.regex.Pattern

class EduroamFixCard(
    context: Context
) : Card(CardManager.CARD_EDUROAM_FIX, context, "card_eduroam_fix_start") {

    private val errors: MutableList<String> = ArrayList()
    private lateinit var eduroam: WifiConfiguration

    private fun isConfigValid(): Boolean {
        errors.clear()
        // If it is not configured then the config valid
        eduroam = EduroamController.getEduroamConfig(context) ?: return true

        // Eduroam was configured by other university
        if (!isTumEduroam(eduroam.enterpriseConfig.identity)) {
            Utils.log("Eduroam wasn't configured at TUM")
            return true
        }

        // Check attributes - check newer match for the radius server
        // for all configurations
        // Check that the full quantifier is used (we already know it's a tum config)
        if (!eduroam.enterpriseConfig.identity.contains(AT_SIGN)) {
            errors.add(context.getString(R.string.wifi_identity_zone))
        }

        val eapMethod = eduroam.enterpriseConfig.eapMethod
        val phase2 = eduroam.enterpriseConfig.phase2Method

        if (eapMethod == TTLS && (phase2 == MSCHAPV2 || phase2 == PAP) || eapMethod == PEAP && phase2 == MSCHAPV2) {
            checkDNSName()
            checkAnonymousIdentity()
            // note: checking the certificate does not seem possible
        }
        // else: PWD or unknown authentication method (we don't know if that method is safe or not -> ignore)

        return errors.isEmpty()
    }

    override fun updateViewHolder(viewHolder: RecyclerView.ViewHolder) {
        if (viewHolder is EduroamFixCardViewHolder) {
            viewHolder.bind(eduroam, errors)
        }
    }

    override fun shouldShow(prefs: SharedPreferences): Boolean {
        // Check if wifi is turned on at all, as we cannot say if it was configured if its off
        return if (!context.wifiManager.isWifiEnabled) {
            false
        } else !isConfigValid()
    }

    override fun discard(editor: SharedPreferences.Editor) {
        context.defaultSharedPreferences.edit().putBoolean("card_eduroam_fix_start", false).apply()
    }

    override fun getId(): Int {
        return 0
    }

    private fun checkAnonymousIdentity() {
        val anonymousIdentity = eduroam.enterpriseConfig.anonymousIdentity
        if (anonymousIdentity != null &&
                anonymousIdentity != "anonymous@mwn.de" &&
                anonymousIdentity != "anonymous@eduroam.mwn.de" &&
                anonymousIdentity != "anonymous@mytum.de") {
            errors.add(context.getString(R.string.wifi_anonymous_identity_not_set))
        }
    }

    private fun checkDNSName() {
        if (SDK_INT < M && !isValidSubjectMatchAPI18(eduroam)) {
            errors.add(context.getString(R.string.wifi_dns_name_not_set))
        } else if (SDK_INT >= M &&
                (eduroam.enterpriseConfig.altSubjectMatch != "DNS:$RADIUS_DNS" || eduroam.enterpriseConfig.domainSuffixMatch != RADIUS_DNS) &&
                !isValidSubjectMatchAPI18(eduroam)) {
            errors.add(context.getString(R.string.wifi_dns_name_not_set))
        }
    }

    private fun isTumEduroam(identity: String): Boolean {
        val pattern = Pattern.compile(Const.TUM_ID_PATTERN)
        return (identity.endsWith("@mwn.de") ||
                identity.endsWith("@mytum.de") ||
                identity.endsWith("@tum.de") ||
                (identity.endsWith(".mwn.de") || identity.endsWith(".tum.de")) && identity.contains(AT_SIGN) ||
                pattern.matcher(identity).matches())
    }

    private fun isValidSubjectMatchAPI18(eduroam: WifiConfiguration): Boolean {
        // AltSubjectMatch is not available for API18
        Utils.log("SubjectMatch: " + eduroam.enterpriseConfig.subjectMatch)
        return eduroam.enterpriseConfig.subjectMatch == RADIUS_DNS
    }

    companion object {
        private const val RADIUS_DNS = "radius.lrz.de"
        private const val AT_SIGN = "@"
        @JvmStatic
        fun inflateViewHolder(parent: ViewGroup, interactionListener: CardInteractionListener): CardViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.card_eduroam_fix, parent, false)
            return EduroamFixCardViewHolder(view, interactionListener)
        }
    }
}
