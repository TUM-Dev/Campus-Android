package de.tum.`in`.tumcampusapp.models.tumcabe

import android.content.Context

import de.tum.`in`.tumcampusapp.auxiliary.NetUtils
import de.tum.`in`.tumcampusapp.trace.G
import de.tum.`in`.tumcampusapp.trace.Util

data class BugReport(val packageName: String, val packageVersion: String, val packageVersionCode: String, val phoneModel: String,
                     val androidVersion: String, val networkWifi: String, val networkMobile: String, val gps: String, val screenWidth: String,
                     val screenHeight: String, val screenOrientation: String, val screenDpi: String, val log: String, val stacktrace: String) {

    companion object {
        fun getBugReport(c: Context, stacktrace: String, log: String): BugReport {
            val screenProperties = Util.getScreenProperties(c)
            return BugReport(
                    packageName = G.appPackage,
                    packageVersion = G.appVersion,
                    packageVersionCode = G.appVersionCode.toString(),
                    phoneModel = G.phoneModel,
                    androidVersion = G.androidVersion,
                    networkWifi = if (NetUtils.isConnectedWifi(c)) "true" else "false",
                    networkMobile = if (NetUtils.isConnectedMobileData(c)) "true" else "false",
                    gps = Util.isGPSOn(c),
                    screenWidth = screenProperties[0],
                    screenHeight = screenProperties[1],
                    screenOrientation = screenProperties[2],
                    screenDpi = screenProperties[3] + ":" + screenProperties[4],
                    stacktrace = stacktrace,
                    log = log
            )
        }
    }
}
