package de.tum.`in`.tumcampusapp.component.ui.eduroam

import android.content.Context
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiEnterpriseConfig
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Utils
import org.jetbrains.anko.wifiManager

class EduroamController(private val context: Context) {

    fun getEduroamConfig(): WifiConfiguration? {
        val wifiManager = context.wifiManager
        val networks = wifiManager.configuredNetworks ?: return null

        val eduroamSSID = "\"${Const.EDUROAM_SSID}\""
        return networks.firstOrNull { it.SSID == eduroamSSID  }
    }

    fun configureEduroam(lrzId: String, networkPass: String): Boolean {
        // Configure Wifi
        val originalConfig = getEduroamConfig()
        val shouldUpdate = originalConfig != null
        val newConfig = getEduroamConfig() ?: WifiConfiguration()

        newConfig.apply {
            SSID = "\"" + Const.EDUROAM_SSID + "\""
            allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP)
            allowedKeyManagement.set(WifiConfiguration.KeyMgmt.IEEE8021X)
            allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP)
            allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP)
            allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP)
            allowedProtocols.set(WifiConfiguration.Protocol.RSN)
            status = WifiConfiguration.Status.ENABLED
        }

        setupEnterpriseConfigAPI18(newConfig, lrzId, networkPass)

        // Add eduroam to wifi networks
        val wifiManager = context.wifiManager
        val networkId = if (shouldUpdate) {
            Utils.log("deleted " + newConfig.networkId)
            wifiManager.updateNetwork(newConfig)
        } else {
            wifiManager.addNetwork(newConfig)
        }
        Utils.log("added $networkId")

        // Check if update successful
        if (networkId == -1) {
            return false
        }

        // Enable and exit
        wifiManager.enableNetwork(networkId, true)
        return true
    }

    private fun setupEnterpriseConfigAPI18(config: WifiConfiguration,
                                           lrzId: String, wifiPassword: String) {
        config.enterpriseConfig.apply {
            identity = "$lrzId@eduroam.mwn.de"
            password = wifiPassword
            eapMethod = WifiEnterpriseConfig.Eap.PWD
        }
    }

}
