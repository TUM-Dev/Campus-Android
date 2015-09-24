package de.tum.in.tumcampus.models.managers;

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

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.auxiliary.Utils;

/**
 * Eduroam manager, manages connecting to eduroam wifi network
 */
public class EduroamManager {
    public static final String networkSSID = "eduroam";
    private static final String INT_PHASE2 = "phase2";
    private static final String INT_PASSWORD = "password";
    private static final String INT_IDENTITY = "identity";
    private static final String INT_EAP = "eap";
    private static final String INT_CA_CERT = "ca_cert";
    private static final String INT_ANONYMOUS_IDENTITY = "anonymous_identity";
    private static final String INT_ENTERPRISE_FIELD_NAME = "android.net.wifi.WifiConfiguration$EnterpriseField";

    private final Context mContext;

    public EduroamManager(Context context) {
        mContext = context;
    }

    /**
     * Tests if eduroam has already been setup
     *
     * @return true if eduroam is already setup, false otherwise
     */
    public boolean isConfigured() {
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        if (list == null)
            return true;
        for (WifiConfiguration i : list) {
            if (i.SSID != null && i.SSID.equals("\"" + networkSSID + "\"")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Configures eduroam wifi connection
     * @param lrzId User's LRZ-ID
     * @param networkPass User's lrz password
     * @return Returns true if configuration was successful, false otherwise
     */
    public boolean configureEduroam(String lrzId, String networkPass) {
        // Configure Wifi
        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = "\"" + networkSSID + "\"";
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
            conf.enterpriseConfig.setIdentity(lrzId + "@eduroam.mwn.de");
            conf.enterpriseConfig.setPassword(networkPass);
            conf.enterpriseConfig.setEapMethod(WifiEnterpriseConfig.Eap.PEAP);
            conf.enterpriseConfig.setPhase2Method(WifiEnterpriseConfig.Phase2.MSCHAPV2);
            conf.enterpriseConfig.setAnonymousIdentity("anonymous@mwn.de");

            // Install certificate
            X509Certificate cert;
            try {
                InputStream is = mContext.getResources().openRawResource(R.raw.rootcert);
                CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
                cert = (X509Certificate) certFactory.generateCertificate(is);
            } catch (CertificateException e) {
                Utils.log(e);
                return false;
            }
            conf.enterpriseConfig.setCaCertificate(cert);
        } else {

            try {
                // Get class instance for enterprise field class and than find setValue Method
                Method wcefSetValue = null;
                Class<?>[] wcClasses = WifiConfiguration.class.getClasses();
                for (Class<?> wcClass : wcClasses) {
                    if (wcClass.getName().equals(INT_ENTERPRISE_FIELD_NAME)) {
                        for (Method m : wcClass.getMethods()) {
                            if (m.getName().trim().equals("setValue")) {
                                wcefSetValue = m;
                                break;
                            }
                        }
                        break;
                    }
                }

                if(wcefSetValue==null)
                    return false;

                Field[] wcefFields = WifiConfiguration.class.getFields();
                for (Field wcefField : wcefFields) {
                        if (wcefField.getName().trim().equals(INT_ANONYMOUS_IDENTITY)) {
                            wcefSetValue.invoke(wcefField.get(conf), "anonymous@mwn.de");
                        } else if (wcefField.getName().trim().equals(INT_CA_CERT)) {
                            wcefSetValue.invoke(wcefField.get(conf), "keystore://CACERT_eduroam");
                        } else if (wcefField.getName().trim().equals(INT_EAP)) {
                            wcefSetValue.invoke(wcefField.get(conf), "PEAP");
                        } else if (wcefField.getName().trim().equals(INT_IDENTITY)) {
                            wcefSetValue.invoke(wcefField.get(conf), lrzId + "@eduroam.mwn.de");
                        } else if (wcefField.getName().trim().equals(INT_PASSWORD)) {
                            wcefSetValue.invoke(wcefField.get(conf), networkPass);
                        } else if (wcefField.getName().trim().equals(INT_PHASE2)) {
                            wcefSetValue.invoke(wcefField.get(conf), "MSCHAPV2");
                        }
                }
            } catch (Exception e) {
                Utils.log(e);
                return false;
            }
        }

        // Add eduroam to wifi networks
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        int networkId = wifiManager.addNetwork(conf);

        if (networkId != -1) {
            wifiManager.saveConfiguration();
        }
        return networkId != -1;
    }
}
