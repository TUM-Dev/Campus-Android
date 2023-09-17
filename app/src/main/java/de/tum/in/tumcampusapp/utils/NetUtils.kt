package de.tum.`in`.tumcampusapp.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET
import android.net.NetworkCapabilities.NET_CAPABILITY_NOT_METERED
import android.net.NetworkCapabilities.NET_CAPABILITY_VALIDATED
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.M

object NetUtils {

    @JvmStatic
    val internetCapability = if (SDK_INT < M) NET_CAPABILITY_INTERNET else NET_CAPABILITY_VALIDATED

    private fun NetworkCapabilities.hasConnectivity(): Boolean {
        return hasCapability(internetCapability)
    }

    private inline fun anyConnectedNetwork(
        context: Context,
        condition: (NetworkCapabilities) -> Boolean = { true }
    ): Boolean {
        val connectivityMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        return connectivityMgr.allNetworks.asSequence().filter {
            connectivityMgr.getNetworkInfo(it)?.isConnected ?: false
        }.mapNotNull {
            connectivityMgr.getNetworkCapabilities(it)
        }.any {
            it.hasConnectivity() && condition(it)
        }
    }

    /**
     * Check if a network connection is available or can be available soon
     *
     * @return if any internet connectivity is available
     */
    @JvmStatic
    fun isConnected(context: Context): Boolean {
        return anyConnectedNetwork(context)
    }

    /**
     * Check if a network connection is available or can be available soon
     * and if the available connection is a wifi internet connection
     *
     * @return if an unmetered connection (probably WiFi) is available
     */
    @JvmStatic
    fun isConnectedWifi(context: Context): Boolean {
        return anyConnectedNetwork(context) { it.hasCapability(NET_CAPABILITY_NOT_METERED) }
    }
}
