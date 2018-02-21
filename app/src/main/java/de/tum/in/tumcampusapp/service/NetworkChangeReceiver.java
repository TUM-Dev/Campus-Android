package de.tum.in.tumcampusapp.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

import de.tum.in.tumcampusapp.utils.Utils;

public class NetworkChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager conManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = conManager.getActiveNetworkInfo();
        if (info == null) {
            return;
        }
        if (info.isConnected()) {
            Utils.log("WifiStateChange");
            WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            wm.startScan();
        }
    }
}
