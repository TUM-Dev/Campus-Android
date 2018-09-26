package de.tum.`in`.tumcampusapp.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET
import android.net.NetworkCapabilities.NET_CAPABILITY_VALIDATED
import android.os.Build

object NetUtils {

    private fun NetworkCapabilities.hasConnectivity(): Boolean {
        val capability = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) NET_CAPABILITY_INTERNET else NET_CAPABILITY_VALIDATED
        return hasCapability(capability)
    }

    private inline fun anyConnectedNetwork(context: Context, condition: (NetworkCapabilities) -> Boolean): Boolean {
        val connectivityMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return false

        return connectivityMgr.allNetworks.asSequence().filter {
            connectivityMgr.getNetworkInfo(it)?.isConnected ?: false
        }.mapNotNull {
            connectivityMgr.getNetworkCapabilities(it)
        }.any {
            condition(it)
        }
    }

    /**
     * Check if a network connection is available or can be available soon
     *
     * @return if any internet connectivity is available
     */
    @JvmStatic
    fun isConnected(context: Context): Boolean {
        return anyConnectedNetwork(context) { it.hasConnectivity() }
    }

    /**
     * Check if a network connection is available or can be available soon
     * and if the available connection is a wifi internet connection
     *
     * @return if an unmetered connection (probably WiFi) is available
     */
    @JvmStatic
    fun isConnectedWifi(context: Context): Boolean {
        return anyConnectedNetwork(context) {
            it.hasConnectivity()
                    && it.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
        }
    }
}
