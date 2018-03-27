package de.tum.in.tumcampusapp.component.ui.eduroam;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.GroupCipher;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiConfiguration.PairwiseCipher;
import android.net.wifi.WifiConfiguration.Protocol;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.WifiManager;

import java.util.List;

import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.Utils;

/**
 * Eduroam manager, manages connecting to eduroam wifi network
 */
public class EduroamController {

    private final Context mContext;

    EduroamController(Context context) {
        mContext = context;
    }

    /**
     * Tests if eduroam has already been setup
     *
     * @return true if eduroam is already setup, false otherwise
     */
    static public WifiConfiguration getEduroamConfig(Context c) {
        WifiManager wifiManager = (WifiManager) c.getApplicationContext()
                                                 .getSystemService(Context.WIFI_SERVICE);
        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();

        //We didn't get a list, so maybe there's no wifi?
        if (list == null) {
            return null;
        }

        for (WifiConfiguration config : list) {
            if (config.SSID != null && config.SSID.equals("\"" + Const.EDUROAM_SSID + "\"")) {
                return config;
            }
        }
        return null;
    }

    /**
     * Configures eduroam wifi connection
     *
     * @param lrzId       User's LRZ-ID
     * @param networkPass User's lrz password
     * @return Returns true if configuration was successful, false otherwise
     */
    boolean configureEduroam(String lrzId, String networkPass) {
        // Configure Wifi
        boolean update = true;
        WifiConfiguration conf = getEduroamConfig(mContext);

        if (conf == null) {
            update = false;
            conf = new WifiConfiguration();
        }

        conf.SSID = "\"" + Const.EDUROAM_SSID + "\"";
        conf.allowedKeyManagement.set(KeyMgmt.WPA_EAP);
        conf.allowedKeyManagement.set(KeyMgmt.IEEE8021X);
        conf.allowedGroupCiphers.set(GroupCipher.TKIP);
        conf.allowedGroupCiphers.set(GroupCipher.CCMP);
        conf.allowedGroupCiphers.set(GroupCipher.WEP40);
        conf.allowedGroupCiphers.set(GroupCipher.WEP104);
        conf.allowedPairwiseCiphers.set(PairwiseCipher.CCMP);
        conf.allowedPairwiseCiphers.set(PairwiseCipher.TKIP);
        conf.allowedProtocols.set(Protocol.RSN);
        conf.status = WifiConfiguration.Status.ENABLED;

        setupEnterpriseConfigAPI18(conf, lrzId, networkPass);

        // Add eduroam to wifi networks
        WifiManager wifiManager = (WifiManager) mContext.getApplicationContext()
                                                        .getSystemService(Context.WIFI_SERVICE);
        int networkId;
        if (update) {
            networkId = wifiManager.updateNetwork(conf);
            Utils.log("deleted " + conf.networkId);
        } else {
            networkId = wifiManager.addNetwork(conf);
        }
        Utils.log("added " + networkId);

        //Check if update successful
        if (networkId == -1) {
            return false;
        }

        //Enable and exit
        wifiManager.enableNetwork(networkId, true);
        return true;
    }

    private void setupEnterpriseConfigAPI18(WifiConfiguration conf, String lrzId, String networkPass) {
        conf.enterpriseConfig.setIdentity(lrzId + "@eduroam.mwn.de");
        conf.enterpriseConfig.setPassword(networkPass);
        conf.enterpriseConfig.setEapMethod(WifiEnterpriseConfig.Eap.PWD);
    }

}
