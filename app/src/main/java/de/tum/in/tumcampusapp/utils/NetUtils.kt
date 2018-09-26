package de.tum.`in`.tumcampusapp.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo

object NetUtils {

    private fun getNetInfo(context: Context) : NetworkInfo? {
        val connectivityMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        return connectivityMgr?.activeNetworkInfo
    }

    /**
     * Check if a network connection is available or can be available soon
     *
     * @return if any internet connectivity is available
     */
    @JvmStatic
    fun isConnected(context: Context): Boolean {
        val netInfo = getNetInfo(context) ?: return false
        return netInfo.isConnectedOrConnecting
    }

    /**
     * Check if a network connection is available or can be available soon
     * and if the available connection is a wifi internet connection
     *
     * @return if a WiFi connection is available
     */
    @JvmStatic
    fun isConnectedWifi(context: Context): Boolean {
        val netInfo = getNetInfo(context) ?: return false
        return netInfo.isConnectedOrConnecting && netInfo.type == ConnectivityManager.TYPE_WIFI
    }
}
