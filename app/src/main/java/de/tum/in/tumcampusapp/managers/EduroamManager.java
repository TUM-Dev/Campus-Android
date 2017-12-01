package de.tum.in.tumcampusapp.managers;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.GroupCipher;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiConfiguration.PairwiseCipher;
import android.net.wifi.WifiConfiguration.Protocol;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.WifiManager;
import android.os.Build;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.auxiliary.Utils;

/**
 * Eduroam manager, manages connecting to eduroam wifi network
 */
public class EduroamManager {
    public static final String NETWORK_SSID = "eduroam";
    public static final String RADIUS_DNS = "radius.lrz.de";
    private static final String INT_PHASE2 = "phase2";
    private static final String INT_PASSWORD = "password";
    private static final String INT_IDENTITY = "identity";
    private static final String INT_EAP = "eap";
    private static final String INT_CA_CERT = "ca_cert";
    private static final String INT_ANONYMOUS_IDENTITY = "anonymous_identity";
    private static final String INT_ENTERPRISE_FIELD_NAME = "android.net.wifi.WifiConfiguration$EnterpriseField";
    private static final String ANON_IDENTITY = "anonymous@eduroam.mwn.de";

    private final Context mContext;

    public EduroamManager(Context context) {
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
            if (config.SSID != null && config.SSID.equals("\"" + NETWORK_SSID + "\"")) {
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
    public boolean configureEduroam(String lrzId, String networkPass) {
        // Configure Wifi
        boolean update = true;
        WifiConfiguration conf = getEduroamConfig(mContext);

        if (conf == null) {
            update = false;
            conf = new WifiConfiguration();
        }

        conf.SSID = "\"" + NETWORK_SSID + "\"";
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

        if (Build.VERSION.SDK_INT >= 18) {
            setupEnterpriseConfigAPI18(conf, lrzId, networkPass);
        } else {
            if (!setupEnterpriseConfigOld(conf, lrzId, networkPass)) {
                return false;
            }
        }

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

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void setupEnterpriseConfigAPI18(WifiConfiguration conf, String lrzId, String networkPass) {
        conf.enterpriseConfig.setIdentity(lrzId + "@eduroam.mwn.de");
        conf.enterpriseConfig.setPassword(networkPass);
        conf.enterpriseConfig.setEapMethod(WifiEnterpriseConfig.Eap.PWD);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setSubjectMatchAPI23(conf);
        }
        setSubjectMatch18To23(conf); //Set both just to be sure
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void setSubjectMatchAPI23(WifiConfiguration conf) {
        conf.enterpriseConfig.setDomainSuffixMatch(RADIUS_DNS);
        conf.enterpriseConfig.setAltSubjectMatch("DNS:" + RADIUS_DNS);
    }

    @TargetApi(18)
    @SuppressWarnings("deprecation")
    private void setSubjectMatch18To23(WifiConfiguration conf) {
        conf.enterpriseConfig.setSubjectMatch(RADIUS_DNS);
    }

    private boolean setupEnterpriseConfigOld(WifiConfiguration conf, String lrzId, String networkPass) {
        try {
            // Get class instance for enterprise field class and than find setValue Method
            Method wcefSetValue = null;
            Class<?>[] wcClasses = WifiConfiguration.class.getClasses();
            for (Class<?> wcClass : wcClasses) {
                if (wcClass.getName()
                           .equals(INT_ENTERPRISE_FIELD_NAME)) {
                    for (Method m : wcClass.getMethods()) {
                        if (m.getName()
                             .trim()
                             .equals("setValue")) {
                            wcefSetValue = m;
                            break;
                        }
                    }
                    break;
                }
            }

            if (wcefSetValue == null) {
                return false;
            }

            Field[] wcefFields = WifiConfiguration.class.getFields();
            for (Field wcefField : wcefFields) {

                if (wcefField.getName().trim().equals(INT_EAP)) {
                    wcefSetValue.invoke(wcefField.get(conf), "PWD");

                } else if (wcefField.getName().trim().equals(INT_IDENTITY)) {
                    wcefSetValue.invoke(wcefField.get(conf), lrzId + "@eduroam.mwn.de");

                } else if (wcefField.getName().trim().equals(INT_PASSWORD)) {
                    wcefSetValue.invoke(wcefField.get(conf), networkPass);
                }
            }
        } catch (Exception e) {
            Utils.log(e);
            return false;
        }
        return true;
    }
}
