package de.tum.`in`.tumcampusapp.service

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import de.tum.`in`.tumcampusapp.utils.Utils

class NetworkChangeReceiver : BroadcastReceiver() {

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val info = connectivityManager.activeNetworkInfo
        if (!info.isConnected) {
            return
        }

        // Use applicationContext because context can lead to memory leaks on devices < Android N
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        Utils.log("WifiStateChange")
        wifiManager.startScan()
    }

}
