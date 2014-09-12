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

import java.util.List;

public class EduroamManager {
    public static final String networkSSID = "eduroam";

    private static final String INT_PRIVATE_KEY = "private_key";
    private static final String INT_PHASE2 = "phase2";
    private static final String INT_PASSWORD = "password";
    private static final String INT_IDENTITY = "identity";
    private static final String INT_EAP = "eap";
    private static final String INT_CLIENT_CERT = "client_cert";
    private static final String INT_CA_CERT = "ca_cert";
    private static final String INT_ANONYMOUS_IDENTITY = "anonymous_identity";
    final String INT_ENTERPRISEFIELD_NAME = "android.net.wifi.WifiConfiguration$EnterpriseField";

    private Context mContext;

    public EduroamManager(Context mContext) {
        this.mContext = mContext;
    }

    /**
     * Tests if eduroam has already been setup
     *
     * @return true if eduroam is already setup, false otherwise
     * */
    public boolean isConfigured() {
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration i : list) {
            if (i.SSID != null && i.SSID.equals("\"" + networkSSID + "\"")) {
                return true;
            }
        }
        return false;
    }

    public void configureEduroam(String lrzId, String networkPass) {

        // Configure Wifi
        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = "\"" + networkSSID + "\"";
        conf.allowedKeyManagement.set(KeyMgmt.IEEE8021X);
        conf.allowedKeyManagement.set(KeyMgmt.WPA_EAP);
        conf.allowedKeyManagement.set(KeyMgmt.WPA_PSK);
        conf.allowedGroupCiphers.set(GroupCipher.TKIP);
        conf.allowedGroupCiphers.set(GroupCipher.CCMP);
        conf.allowedGroupCiphers.set(GroupCipher.WEP40);
        conf.allowedGroupCiphers.set(GroupCipher.WEP104);
        conf.allowedPairwiseCiphers.set(PairwiseCipher.CCMP);
        conf.allowedPairwiseCiphers.set(PairwiseCipher.TKIP);
        conf.allowedProtocols.set(Protocol.RSN);
        if(Build.VERSION.SDK_INT>=18) {
            conf.enterpriseConfig.setIdentity(lrzId + "@eduroam.mwn.de");
            conf.enterpriseConfig.setPassword(networkPass);
            conf.enterpriseConfig.setEapMethod(WifiEnterpriseConfig.Eap.PEAP);
            conf.enterpriseConfig.setPhase2Method(WifiEnterpriseConfig.Phase2.MSCHAPV2);
            //TODO support for API lower than 18
        } /*else {
            try
            {
                // Let the magic start
                Class[] wcClasses = WifiConfiguration.class.getClasses();
                // null for overzealous java compiler
                Class wcEnterpriseField = null;

                for (Class wcClass : wcClasses)
                    if (wcClass.getName().equals(INT_ENTERPRISEFIELD_NAME))
                    {
                        wcEnterpriseField = wcClass;
                        break;
                    }
                boolean noEnterpriseFieldType = false;
                if(wcEnterpriseField == null)
                    noEnterpriseFieldType = true; // Cupcake/Donut access enterprise settings directly

                Field wcefAnonymousId = null, wcefCaCert = null, wcefClientCert = null, wcefEap = null, wcefIdentity = null, wcefPassword = null, wcefPhase2 = null, wcefPrivateKey = null;
                Field[] wcefFields = WifiConfiguration.class.getFields();
                // Dispatching Field vars
                for (Field wcefField : wcefFields)
                {
                    if (wcefField.getName().trim().equals(INT_ANONYMOUS_IDENTITY))
                        wcefAnonymousId = wcefField;
                    else if (wcefField.getName().trim().equals(INT_CA_CERT))
                        wcefCaCert = wcefField;
                    else if (wcefField.getName().trim().equals(INT_CLIENT_CERT))
                        wcefClientCert = wcefField;
                    else if (wcefField.getName().trim().equals(INT_EAP))
                        wcefEap = wcefField;
                    else if (wcefField.getName().trim().equals(INT_IDENTITY))
                        wcefIdentity = wcefField;
                    else if (wcefField.getName().trim().equals(INT_PASSWORD))
                        wcefPassword = wcefField;
                    else if (wcefField.getName().trim().equals(INT_PHASE2))
                        wcefPhase2 = wcefField;
                    else if (wcefField.getName().trim().equals(INT_PRIVATE_KEY))
                        wcefPrivateKey = wcefField;
                }
                Method wcefSetValue = null;
                if(!noEnterpriseFieldType)
                {
                    for(Method m: wcEnterpriseField.getMethods())
                        //System.out.println(m.getName());
                        if(m.getName().trim().equals("value")){
                            wcefSetValue = m;
                            break;
                        }
                }

                //*EAP Method*//*
                String result = null;
                Object obj = null;
                if(!noEnterpriseFieldType)
                {
                    obj = wcefSetValue.invoke(wcefEap.get(config), null);
                    String retval = (String)obj;
                }

                //*phase 2*//*
                if(!noEnterpriseFieldType)
                {
                    result = (String) wcefSetValue.invoke(wcefPhase2.get(config), null);
                }

                //*Anonymous Identity*//*
                if(!noEnterpriseFieldType)
                {
                    result = (String) wcefSetValue.invoke(wcefAnonymousId.get(config),null);
                }

                //*CA certificate*//*
                if(!noEnterpriseFieldType)
                {
                    result = (String) wcefSetValue.invoke(wcefCaCert.get(config), null);
                }

                //*private key*//*
                if(!noEnterpriseFieldType)
                {
                    result = (String) wcefSetValue.invoke(wcefPrivateKey.get(config),null);
                }

                //*Identity*//*
                if(!noEnterpriseFieldType)
                {
                    result = (String) wcefSetValue.invoke(wcefIdentity.get(config), null);
                }

                //*Password*//*
                if(!noEnterpriseFieldType)
                {
                    result = (String) wcefSetValue.invoke(wcefPassword.get(config), null);
                }

                //*client certificate*//*
                if(!noEnterpriseFieldType)
                {
                    result = (String) wcefSetValue.invoke(wcefClientCert.get(config), null);
                }

            }
            catch(IOException e)
            {
                Log.e("<<<<<<<<<<WifiPreference>>>>>>>>>>>>", "Could not write to ReadConfigLog.txt" + e.getMessage());
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }*/


        // Add eduroam to wifi networks
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        wifiManager.addNetwork(conf);

        //And finally,you might need to enable it, so Android conntects to it:
        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration i : list) {
            if (i.SSID != null && i.SSID.equals("\"" + networkSSID + "\"")) {
                //wifiManager.disconnect();
                wifiManager.enableNetwork(i.networkId, true);
                //wifiManager.reconnect();
                break;
            }
        }
    }
}
